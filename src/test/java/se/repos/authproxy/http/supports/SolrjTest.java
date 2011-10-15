package se.repos.authproxy.http.supports;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import javax.servlet.Filter;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScheme;
import org.apache.commons.httpclient.auth.CredentialsNotAvailableException;
import org.apache.commons.httpclient.auth.CredentialsProvider;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
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
		int port = 49999; // TODO random test port
		Server server = new Server(port);
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		server.setHandler(context);
		context.addServlet(new ServletHolder(new HelloServlet("SvnKit test")), "/*");
		Filter filter = new ReposRequireLoginFilter();
		FilterHolder holder = new FilterHolder(filter);
		holder.setInitParameter("realm", "See-if-solr-detects-this-realm");
		context.addFilter(holder, "/*", 0);
		server.start();
		
		SolrServer solrj = new CommonsHttpSolrServer("http://localhost:" + port + "/solr");
		ReposCurrentUser user = mock(ReposCurrentUser.class);
		when(user.isAuthenticated()).thenReturn(false);
		AuthProxySolrjAuthentication auth = new AuthProxySolrjAuthentication((CommonsHttpSolrServer) solrj, user);
		try {
			solrj.query(new SolrQuery("*:*"));
		} catch (SolrServerException e) {
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
		public void analyze(SolrServerException e) throws AuthFailedException {
			if (e.getCause() instanceof org.apache.solr.common.SolrException &&
					e.getCause().getMessage().startsWith("Unauthorized")) {
				throw new AuthFailedException("solrj", lastRealm);
			}
		}
		
		public AuthProxySolrjAuthentication(CommonsHttpSolrServer solrjServer, final ReposCurrentUser user) {
			HttpClient httpClient = solrjServer.getHttpClient();
			// see httpclient 3.1 InteractiveAuthenticationExample
			httpClient.getParams().setParameter(CredentialsProvider.PROVIDER, new CredentialsProvider() {
				@Override
				public Credentials getCredentials(AuthScheme scheme, String host, int port, boolean proxy) 
						throws CredentialsNotAvailableException {
					lastRealm = scheme.getRealm();
					if (!user.isAuthenticated()) {
						throw new CredentialsNotAvailableException();
					}
					return new UsernamePasswordCredentials(user.getUsername(), user.getPassword());
				}
			});
		}
		
	}
	
}
