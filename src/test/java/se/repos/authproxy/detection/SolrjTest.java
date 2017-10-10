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
package se.repos.authproxy.detection;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import javax.servlet.Filter;

import org.apache.http.auth.Credentials;
import org.apache.http.client.HttpClient;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.eclipse.jetty.embedded.HelloServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.Test;

import se.repos.authproxy.AuthFailedException;
import se.repos.authproxy.ReposCurrentUser;
import se.repos.authproxy.http.ReposRequireLoginFilter;

public class SolrjTest {

	@Test
	public void testToReposAuthFailedException() throws Exception {
		// No need for a solr server, just require authentcation
		int port = 49992; // TODO random test port
		Server server = new Server(port);
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		server.setHandler(context);
		context.addServlet(new ServletHolder(new HelloServlet("Solrj test")), "/*");
		Filter filter = new ReposRequireLoginFilter();
		FilterHolder holder = new FilterHolder(filter);
		holder.setInitParameter("realm", "See-if-solr-detects-this-realm");
		context.addFilter(holder, "/*", 0);
		server.start();
		
		SolrServer solrj = new HttpSolrServer("http://localhost:" + port + "/solr");
		ReposCurrentUser user = mock(ReposCurrentUser.class);
		when(user.isAuthenticated()).thenReturn(false);
		AuthProxySolrjAuthentication auth = new AuthProxySolrjAuthentication((HttpSolrServer) solrj, user);
		try {
			solrj.query(new SolrQuery("*:*"));
		//solrj3: } catch (SolrServerException e) {
		} catch (org.apache.solr.common.SolrException e) {
			try {
				auth.analyze(e);
				fail("Should have identified authentication error");
			} catch (AuthFailedException a) {
				assertEquals("See-if-solr-detects-this-realm", a.getRealm());
			}
		}
		when(user.isAuthenticated()).thenReturn(true);
		when(user.getUsername()).thenReturn("user");
		when(user.getPassword()).thenReturn("pass");
		try {
			solrj.query(new SolrQuery("*:*"));
		} catch (SolrServerException e) {
			assertEquals("Authentication should now have passed", 
					"Invalid version (expected 2, but 60) or the data in not in 'javabin' format",
					e.getCause().getMessage());
		}
	}

	/**
	 * Sample customization of Solrj to do authentication using {@link ReposCurrentUser}
	 * and report failures <em>with</em> realm.
	 * Primitive implementation that remembers last realm per instance (not thread safe)
	 * and does not validate realm, host etc (could send credentials when inappropriate).
	 */
	public class AuthProxySolrjAuthentication {
		
		private String lastRealm = null;
		
		/**
		 * Like {@link AuthFailedException#analyze(Exception)} but for Solrj.
		 */
		//solrj3: public void analyze(SolrServerException e) throws AuthFailedException {
		public void analyze(org.apache.solr.common.SolrException e) throws AuthFailedException {
			System.out.println("Here: " + e.getMessage());
			//solrj3: if (e.getCause() instanceof org.apache.solr.common.SolrException &&
			if (e instanceof org.apache.solr.common.SolrException &&
					//solrj3: e.getCause().getMessage().startsWith("Unauthorized")) {
					//solrj4: org.apache.solr.common.SolrException: Server at http://localhost:49992/solr returned non ok status:401, message:Unauthorized
					e.getMessage().endsWith("status:401, message:Unauthorized")) {
					//e.getMessage().contains("Error 401 Unauthorized")) {
				throw new AuthFailedException("solrj", lastRealm);
			}
		}
		
		public AuthProxySolrjAuthentication(HttpSolrServer solrjServer, final ReposCurrentUser user) {
			HttpClient httpClient = solrjServer.getHttpClient();
			//solrj3: httpClient.getParams().setParameter(CredentialsProvider.PROVIDER, 
			((AbstractHttpClient) httpClient).setCredentialsProvider(new CredentialsProvider() {
				@Override
				public void clear() {
				}
				@Override
				public Credentials getCredentials(AuthScope authscope) {
					lastRealm = authscope.getRealm();
					if (!user.isAuthenticated()) {
						//solrj3: throw new CredentialsNotAvailableException();
						return null;
					}
					return new UsernamePasswordCredentials(user.getUsername(), user.getPassword());
				}
				@Override
				public void setCredentials(AuthScope authscope, Credentials arg1) {
				}
			});
		}
		
	}
	
}
