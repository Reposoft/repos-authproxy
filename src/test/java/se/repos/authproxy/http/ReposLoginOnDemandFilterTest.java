/**
 * Copyright (C) 2004-2012 Repos Mjukvara AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.repos.authproxy.http;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;

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
import org.junit.Ignore;
import org.junit.Test;

import se.repos.authproxy.AuthFailedException;
import se.repos.authproxy.AuthRequiredException;
import se.repos.authproxy.ReposCurrentUser;
import se.repos.restclient.HttpStatusError;
import se.repos.restclient.ResponseHeaders;
import se.repos.restclient.RestAuthentication;
import se.repos.restclient.RestClient;
import se.repos.restclient.RestResponseBean;
import se.repos.restclient.hc.RestClientHc;
import se.repos.restclient.javase.RestClientJavaHttp;

public class ReposLoginOnDemandFilterTest {

	@Ignore // filter not implemented yet
	@Test
	public void testClear() throws IOException, ServletException {
		Filter filter = new ReposLoginOnDemandFilter();
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
	@Ignore // filter not implemented yet
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
				throw new HttpStatusError("http://localhost:4999/c", headers, "failed");
			}
		}), "/c");
		context.addServlet(new ServletHolder(new HttpServlet() {
			@Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
					throws ServletException, IOException {
				ResponseHeaders headers = mock(ResponseHeaders.class);
				when(headers.getStatus()).thenReturn(401);
				when(headers.get("WWW-Authenticate")).thenReturn(Arrays.asList("Basic realm=\"Test realm\""));
				throw new HttpStatusError("http://localhost:4999/d", headers, "failed");
			}
		}), "/d");
		
		Filter filter = new ReposLoginOnDemandFilter();
		FilterHolder holder = new FilterHolder(filter);
		holder.setInitParameter("realm", "Test realm");
		//holder.setInitParameter("currentUserImpl", ""); // Needs to be same instance?
		context.addFilter(holder, "/*", 0);

		server.start();
		
		RestClient client = new RestClientJavaHttp("http://localhost:" + port + "", null);
		RestClient clientWithAuth = new RestClientHc("http://localhost:" + port + "", new RestAuthentication() {
			@Override public String getUsername(String u, String e, String a) { return "name"; }
			@Override public SSLContext getSSLContext(String root) { return null; }
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
	
	/**
	 * Many frameworks wrap all exceptions, including runtime.
	 */
	@Test
	@Ignore
	public void testWrappedAuthFailedException() throws IOException, ServletException {
		ReposLoginOnDemandFilter filter = new ReposLoginOnDemandFilter();
		FilterConfig config = mock(FilterConfig.class);
		filter.init(config);
		
		HttpServletRequest req = mock(HttpServletRequest.class);
		HttpServletResponse resp = mock(HttpServletResponse.class);
		
		AuthRequiredException f = new AuthRequiredException("", "got the realm");
		final Exception e = new Exception(f);
		FilterChain chain = new FilterChain() {
			@Override
			public void doFilter(ServletRequest request, ServletResponse response)
					throws IOException, ServletException {
				throw new ServletException(e);
			}
		};
		
		try {
			filter.doFilter(req, resp, chain);
		} catch (Exception x) {
			fail("Should detect the authentication error and prompt, got: " + x);
		}
		verify(resp).sendError(401);
		// TODO verify that the auth header has the right realm
	}	

}
