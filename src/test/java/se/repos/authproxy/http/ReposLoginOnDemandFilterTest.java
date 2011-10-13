package se.repos.authproxy.http;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import javax.net.ssl.SSLSocketFactory;
import javax.servlet.Filter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.Test;

import se.repos.authproxy.AuthFailedException;
import se.repos.authproxy.AuthRequiredException;
import se.repos.restclient.HttpStatusError;
import se.repos.restclient.ResponseHeaders;
import se.repos.restclient.RestAuthentication;
import se.repos.restclient.RestResponseBean;
import se.repos.restclient.javase.RestClientJavaNet;

public class ReposLoginOnDemandFilterTest {

	/**
	 * Should be duplicated in {@link ReposLoginOnDemandFilterTest} to clarify differences.
	 */
	@SuppressWarnings("serial")
	@Test
	public void testSignalsFromBackend() throws Exception {
		int port = 49999; // TODO random test port
		Server server = new Server(port);
 
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		server.setHandler(context);
		
		context.addServlet(new ServletHolder(new HttpServlet() {
			@Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
					throws ServletException, IOException {
				throw new AuthFailedException("Service auth failed");
			}
		}), "/a");
		context.addServlet(new ServletHolder(new HttpServlet() {
			@Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
					throws ServletException, IOException {
				if (req.getHeader("Authorization") == null) {
					throw new AuthRequiredException("", "A realm"); // Stuck. Realm not given, HTTP not stateful -> realm never known after authentication
				}
				throw new AuthFailedException("Service auth failed");
			}
		}), "/a2");		
		context.addServlet(new ServletHolder(new HttpServlet() {
			@Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
					throws ServletException, IOException {
				throw new AuthRequiredException("No username+password, how odd", "Some realm");
			}
		}), "/b");		
		context.addServlet(new ServletHolder(new HttpServlet() {
			@Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
					throws ServletException, IOException {
				ResponseHeaders headers = mock(ResponseHeaders.class);
				when(headers.getStatus()).thenReturn(401);
				when(headers.get("WWW-Authenticate")).thenReturn(Arrays.asList("Basic realm=\"Other realm\""));
				throw new HttpStatusError(new URL("http://localhost:4999/c"), headers, "failed");
			}
		}), "/c");
		context.addServlet(new ServletHolder(new HttpServlet() {
			@Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
					throws ServletException, IOException {
				ResponseHeaders headers = mock(ResponseHeaders.class);
				when(headers.getStatus()).thenReturn(401);
				when(headers.get("WWW-Authenticate")).thenReturn(Arrays.asList("Basic realm=\"Test realm\""));
				throw new HttpStatusError(new URL("http://localhost:4999/d"), headers, "failed");
			}
		}), "/d");
		
		//Filter filter = new DebugFilter(); // had issues with malfunctioning filter hanging the test
		Filter filter = new ReposRequireLoginFilter();
		FilterHolder holder = new FilterHolder(filter);
		holder.setInitParameter("realm", "Test realm");
		context.addFilter(holder, "/*", 0);

		server.start();
		
		RestClientJavaNet client = new RestClientJavaNet("http://localhost:" + port + "", null);
		RestClientJavaNet clientWithAuth = new RestClientJavaNet("http://localhost:" + port + "", new RestAuthentication() {
			@Override public String getUsername(String u, String e, String a) { return "name"; }
			@Override public SSLSocketFactory getSSLSocketFactory(String root) { return null; }
			@Override public String getPassword(String u, String e, String a, String n) { return "pass"; }
		});
		RestResponseBean resp = new RestResponseBean();
		
		try {
			// No authentication + AuthFailedException
			try {
				client.get("/a", resp);
				fail("Client should get status != 200");
			} catch (HttpStatusError e) {
				assertEquals("Should be a server error because realm is not known", 500, e.getHttpStatus());
				// TODO expect exception message in body?
			}
			// Authentication + AuthFailedException -- 401, reuse realm from request (assume it was validated using ReposCurrentUser)
			try {
				clientWithAuth.get("/a2", resp);
				fail("Client should get status != 200");
			} catch (HttpStatusError e) {
				assertEquals("Should get authentication failed", 401, e.getHttpStatus());
				assertEquals("Should be the configured realm", "Basic realm=\"Test realm\"", e.getHeaders().get("WWW-Authenticate").get(0));
			}
			// No authentication + AuthRequiredException -- not possible with Require filter
			// Authentication + AuthRequiredException -- server error because backend should not behave that way

			// RestClient error without realm or with realm that does not match init param 
			//  -- we could simply ignore this but it is an odd condition so we warn

			// RestClient error with realm -- require authentication as with AuthFailedException

		} finally {
			server.stop();
		}
	}

}
