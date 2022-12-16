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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.embedded.HelloServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.Test;

import se.repos.authproxy.ReposCurrentUser;
import se.repos.restclient.HttpStatusError;
import se.repos.restclient.RestResponseBean;
import se.repos.restclient.javase.RestClientJavaHttp;

public class ReposRequireRemoteuserFilterTest {

	@Test
	public void testFilterJetty() throws Exception {
		// http://wiki.eclipse.org/Jetty/Tutorial/Embedding_Jetty
		
		int port = 49993; // TODO random test port
		Server server = new Server(port);
 
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		server.setHandler(context);
		
		context.addServlet(new ServletHolder(new HelloServlet("Require login test")), "/*");
		//Filter filter = new DebugFilter(); // had issues with malfunctioning filter hanging the test
		Filter filter = new ReposRequireRemoteuserFilter();
		FilterHolder holder = new FilterHolder(filter);
		holder.setInitParameter("realm", "Test realm");
		context.addFilter(holder, "/*", 0);

		server.start();
		
		RestClientJavaHttp client = new RestClientJavaHttp("http://localhost:" + port + "", null);
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
			assertNull(auth);
		} finally {
			server.stop();
		}
	}

	@Test
	public void testFilterAuthenticated() throws Exception {
		
		int port = 49993; // TODO random test port
		Server server = new Server(port);
 
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		server.setHandler(context);
		
		context.addServlet(new ServletHolder(new UserServlet("Require login test")), "/*");
		//Filter filter = new DebugFilter(); // had issues with malfunctioning filter hanging the test
		Filter filter = new ReposRequireRemoteuserFilter();
		FilterHolder holder = new FilterHolder(filter);
		holder.setInitParameter("realm", "Test realm");
		context.addFilter(holder, "/*", 0);

		server.start();
		
		HashMap<String, String> reqHeaders = new HashMap<String, String>();
		reqHeaders.put("X-Forwarded-User", "theusersub");
		RestClientJavaHttp client = new RestClientJavaHttp("http://localhost:" + port + "", null);
		RestResponseBean resp = new RestResponseBean();
		try {
			URL url = new URL("http", "localhost", port, "/");
			client.get(url, resp, reqHeaders);
			assertEquals("<h1>Require login test 'theusersub:'</h1>\n", resp.getBody());
		} catch (HttpStatusError e) {
			fail(Integer.toString(e.getHttpStatus()));
		} finally {
			server.stop();
		}
	}
	
	
	public class UserServlet extends HttpServlet
	{
	    String greeting = "Hello";
	    ReposCurrentUser currentUser = ReposCurrentUser.DEFAULT;

	    public UserServlet()
	    {
	    }

	    public UserServlet(String hi)
	    {
	        greeting = hi;
	    }

	    @Override
	    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	    {
	        response.setContentType("text/html");
	        response.setStatus(HttpServletResponse.SC_OK);
	        response.getWriter().println("<h1>" + greeting + " '" + currentUser.getUsername() + ":" + currentUser.getPassword() + "'</h1>");
	    }
	}
}
