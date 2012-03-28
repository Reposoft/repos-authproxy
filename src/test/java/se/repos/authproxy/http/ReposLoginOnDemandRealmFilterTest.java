package se.repos.authproxy.http;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import javax.net.ssl.SSLContext;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
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
import se.repos.authproxy.ReposCurrentUser;
import se.repos.restclient.HttpStatusError;
import se.repos.restclient.RestAuthentication;
import se.repos.restclient.RestClient;
import se.repos.restclient.RestResponseBean;
import se.repos.restclient.hc.RestClientHc;
import se.repos.restclient.javase.RestClientJavaNet;

/**
 * Prompts for authentication only when a service has required it
 * but uses a configured realm so services don't need to detect realm. 
 */
public class ReposLoginOnDemandRealmFilterTest {
	
	@Test
	public void testClear() throws IOException, ServletException {
		Filter filter = new ReposLoginOnDemandRealmFilter();
		FilterConfig config = mock(FilterConfig.class);
		when(config.getInitParameter("realm")).thenReturn("testClear");
		filter.init(config);
		final ReposCurrentUser currentUser = ReposCurrentUser.DEFAULT; // same as filter
		HttpServletRequest req = mock(HttpServletRequest.class);
		HttpServletResponse resp = mock(HttpServletResponse.class);
		when(req.getHeader("Authorization")).thenReturn("Basic dGVzdDp0ZXN0");
		FilterChain chain = new FilterChain() {
			@Override
			public void doFilter(ServletRequest request, ServletResponse response)
					throws IOException, ServletException {
				assertTrue("Should have authentication during servlet execution", currentUser.isAuthenticated());
			}
		};
		filter.doFilter(req, resp, chain);
		assertFalse("should clear authentication after request processing has completed so it is not reused", currentUser.isAuthenticated());
		
		FilterChain chainEx = new FilterChain() {
			@Override
			public void doFilter(ServletRequest request, ServletResponse response)
					throws IOException, ServletException {
				assertTrue(currentUser.isAuthenticated());
				throw new ServletException("test clear ex");
			}
		};
		try {
			filter.doFilter(req, resp, chainEx);
		} catch (ServletException ex) {
			assertEquals("test clear ex", ex.getMessage());
		}
		assertFalse("should clear after servlet exception", currentUser.isAuthenticated());
		
		FilterChain chainRe = new FilterChain() {
			@Override
			public void doFilter(ServletRequest request, ServletResponse response)
					throws IOException, ServletException {
				assertTrue(currentUser.isAuthenticated());
				throw new RuntimeException("test clear re");
			}
		};
		try {
			filter.doFilter(req, resp, chainRe);
		} catch (RuntimeException re) {
			assertEquals("test clear re", re.getMessage());
		}
		assertFalse("should clear after any exception from filter chain", currentUser.isAuthenticated());		
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
				resp.getOutputStream().write("ok".getBytes());
			}
		}), "/noauth");	
		
		context.addServlet(new ServletHolder(new HttpServlet() {
			@Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
					throws ServletException, IOException {
				if (req.getHeader("Authorization") == null) {
					throw new AuthFailedException("Service auth required");
				} else {
					// TODO Validate password
				}
			}
		}), "/norealm");
		
		context.addServlet(new ServletHolder(new HttpServlet() {
			@Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
					throws ServletException, IOException {
				if (req.getHeader("Authorization") == null) {
					throw new AuthRequiredException("Service auth required", "A realm");
				} else {
					// TODO Validate password so both success and failure can be tested.
					// Must provide a realm for failures too, if provided when there are no credentials
					// because HTTP is stateless and can not remember the realm from before the retry
					throw new AuthFailedException("Service auth failed", "A realm");
				}
			}
		}), "/arealm");
		
		Filter filter = new ReposLoginOnDemandRealmFilter();
		FilterHolder holder = new FilterHolder(filter);
		holder.setInitParameter("realm", "Test realm");
		context.addFilter(holder, "/*", 0);

		server.start();
		
		RestClient client = new RestClientJavaNet("http://localhost:" + port + "", null);
		RestClient clientWithAuth = new RestClientHc("http://localhost:" + port + "", new RestAuthentication() {
			@Override public String getUsername(String u, String e, String a) { return "name"; }
			@Override public SSLContext getSSLContext(String root) { return null; }
			@Override public String getPassword(String u, String e, String a, String n) { return "pass"; }
		});
		RestResponseBean resp = new RestResponseBean();
		
		try {
			
			client.get("/noauth", resp);
			assertEquals("Should not interfere with services that don't require auth", 
					200, resp.getHeaders().getStatus());
			
			// Attempt without authentication to service that requires it
			try {
				client.get("/norealm", resp);
				fail("When auth is required, client should get status != 200");
			} catch (HttpStatusError e) {
				assertEquals("This filter should have configured realm and thus not require realm from service falures",
						401, e.getHttpStatus());
				assertEquals("Basic realm=\"Test realm\"", e.getHeaders().get("WWW-Authenticate").get(0));
			}

			// Successful auth
			clientWithAuth.get("/norealm", resp);
			assertEquals(200, resp.getHeaders().getStatus());
			
			// Service that does detect realm
			try {
				client.get("/arealm", resp);
				fail("When auth is required, client should get status != 200");
			} catch (HttpStatusError e) {
				assertEquals("This filter should have configured realm and thus not require realm from service falures",
						401, e.getHttpStatus());
				assertEquals("Should use the configured realm even if service detects a different one", 
						"Basic realm=\"Test realm\"", e.getHeaders().get("WWW-Authenticate").get(0));
			}
			
			// Authentication failure with realm
			try {
				clientWithAuth.get("/arealm", resp);
				fail("When auth fails, client should get status != 200");
			} catch (HttpStatusError e) {
				assertEquals("This filter should have configured realm and thus not require realm from service falures",
						401, e.getHttpStatus());
				assertEquals("Should use the configured realm even if service detects a different one", 
						"Basic realm=\"Test realm\"", e.getHeaders().get("WWW-Authenticate").get(0));
			}
			
		} finally {
			server.stop();
		}
	}

}
