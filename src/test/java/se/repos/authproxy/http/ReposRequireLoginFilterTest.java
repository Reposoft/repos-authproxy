package se.repos.authproxy.http;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.SSLSocketFactory;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.embedded.HelloServlet;
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
import se.repos.restclient.RestClient;
import se.repos.restclient.RestResponseBean;
import se.repos.restclient.hc.RestClientHc;
import se.repos.restclient.javase.RestClientJavaNet;

public class ReposRequireLoginFilterTest {

	@Test
	public void testFilterJetty() throws Exception {
		// http://wiki.eclipse.org/Jetty/Tutorial/Embedding_Jetty
		
		int port = 49999; // TODO random test port
		Server server = new Server(port);
 
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		server.setHandler(context);
		
		context.addServlet(new ServletHolder(new HelloServlet("Require login test")), "/*");
		//Filter filter = new DebugFilter(); // had issues with malfunctioning filter hanging the test
		Filter filter = new ReposRequireLoginFilter();
		FilterHolder holder = new FilterHolder(filter);
		holder.setInitParameter("realm", "Test realm");
		context.addFilter(holder, "/*", 0);

		server.start();
		
		RestClientJavaNet client = new RestClientJavaNet("http://localhost:" + port + "", null);
		RestResponseBean resp = new RestResponseBean();
		try {
			client.get("/", resp);
			fail("Request should be intercepted by filter that requires authentication");
		} catch (HttpStatusError e) {
			assertEquals(401, e.getHttpStatus());
			assertNull("Response headers are still null", resp.getHeaders()); // Should we set these too?
			assertNotNull(e.getHeaders());
			assertEquals("Should get authentication required", 401, e.getHeaders().getStatus());
			List<String> auth = e.getHeaders().get("WWW-Authenticate");
			assertEquals(1, auth.size());
			assertEquals("Basic realm=\"Test realm\"", auth.get(0));
		} finally {
			server.stop();
		}
	}

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
				throw new AuthFailedException("Service auth failed, no realm");
			}
		}), "/a");
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
		
		RestClient client = new RestClientJavaNet("http://localhost:" + port + "", null);
		RestClient clientWithAuth = new RestClientHc("http://localhost:" + port + "", new RestAuthentication() {
			@Override public String getUsername(String u, String e, String a) { return "name"; }
			@Override public SSLSocketFactory getSSLSocketFactory(String root) { return null; }
			@Override public String getPassword(String u, String e, String a, String n) { return "pass"; }
		});
		RestResponseBean resp = new RestResponseBean();
		
		try {
			// No authentication, "require" filter shouldn't proceed to backend
			try {
				client.get("/a", resp);
				fail("Client should get status != 200");
			} catch (HttpStatusError e) {
				assertEquals("Should get authentication required", 401, e.getHttpStatus());
				assertEquals("Should be the configured realm", "Basic realm=\"Test realm\"", e.getHeaders().get("WWW-Authenticate").get(0));
				// TODO expect exception message in body?
			}
			// Authentication + AuthFailedException -- still 401, ok to have identical response as above
			try {
				clientWithAuth.get("/a", resp);
				fail("Client should get status != 200");
			} catch (HttpStatusError e) {
				assertEquals("Should get authentication failed (retry)", 401, e.getHttpStatus());
				assertEquals("Should be the configured realm", "Basic realm=\"Test realm\"", e.getHeaders().get("WWW-Authenticate").get(0));
			}
			// No authentication + AuthRequiredException -- not possible with Require filter
			// Authentication + AuthRequiredException -- server error because backend should not behave that way
			try {
				clientWithAuth.get("/b", resp);
				fail("Client should get status != 200");
			} catch (HttpStatusError e) {
				assertEquals("Backend misbehaves so this should be a server error", 500, e.getHttpStatus());
			}
			// RestClient error without realm or with realm that does not match init param 
			//  -- we could simply ignore this but it is an odd condition so we warn
			try {
				clientWithAuth.get("/c", resp);
				fail("Client should get status != 200");
			} catch (HttpStatusError e) {
				assertEquals("Authentication config error", 500, e.getHttpStatus());
				assertTrue("Got: " + e, e.getResponse().contains("Authentication settings error"));
				assertTrue("Got: " + e, e.getResponse().contains("Configured realm \"Test realm\" does not match backend's \"Other realm\""));
			}
			// RestClient error with realm -- require authentication as with AuthFailedException
			try {
				clientWithAuth.get("/d", resp);
				fail("Client should get status != 200");
			} catch (HttpStatusError e) {
				assertEquals("Should get authentication required", 401, e.getHttpStatus());
				assertEquals("Should be the configured realm", "Basic realm=\"Test realm\"", e.getHeaders().get("WWW-Authenticate").get(0));
			}			
		} finally {
			server.stop();
		}
	}
	
	@Test
	public void testBrowserCancel() {
		// When a user clicks cancel at the authentication prompt, browsers display the 401 page body
		// We could show different texts for AuthRequired and AuthFailed
		// (for the "require login" filter that's the difference between no auth given and AuthFailed from backend)
	}
	
	class DebugFilter implements Filter {
		@Override public void init(FilterConfig filterConfig) throws ServletException {
			System.out.println("filter init");
		}
		@Override public void doFilter(ServletRequest request, ServletResponse response,
				FilterChain chain) throws IOException, ServletException {
			System.out.println("doFitler");
			chain.doFilter(request, response);
		}
		@Override public void destroy() {
			System.out.println("destroy");
		}
	}
	
}
