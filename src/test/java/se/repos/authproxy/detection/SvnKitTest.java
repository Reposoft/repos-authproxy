package se.repos.authproxy.detection;

import static org.junit.Assert.*;

import javax.servlet.Filter;

import org.eclipse.jetty.embedded.HelloServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.Test;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import se.repos.authproxy.AuthDetection;
import se.repos.authproxy.AuthFailedException;
import se.repos.authproxy.http.ReposRequireLoginFilter;

/**
 * Test that svn authentication and authorization errors
 * can be translated to {@link AuthFailedException}
 * <em>with realm</em>.
 * If we can't get realm the authproxy would have to do
 * an extra HTTP request before it can ask for authentication.
 */
public class SvnKitTest {

	@Test
	public void testToReposAuthFailedException() throws Exception  {
		AuthDetection authDetection = AuthDetection.all;
		
		// No need for a subversion server, just require authentcation
		int port = 49991; // TODO random test port
		Server server = new Server(port);
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		server.setHandler(context);
		context.addServlet(new ServletHolder(new HelloServlet("SvnKit test")), "/*");
		Filter filter = new ReposRequireLoginFilter();
		FilterHolder holder = new FilterHolder(filter);
		holder.setInitParameter("realm", "See-if-svnkit-detects-this-realm");
		context.addFilter(holder, "/*", 0);
		server.start();
		
		// Test that authentication failure and realm can be detected
		try {
			DAVRepositoryFactory.setup();
			SVNRepository repository = SVNRepositoryFactory.create(SVNURL.parseURIDecoded("http://localhost:" + port + "/svn"));
			ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager();
			repository.setAuthenticationManager(authManager);
			repository.info("/", SVNRevision.HEAD.getNumber());
		} catch (SVNException e) {
			try {
				authDetection.analyze(e);
				fail("Should have detected authentication failure from " + e);
			} catch (AuthFailedException a) {
				assertSame(e, a.getCause());
				assertEquals("See-if-svnkit-detects-this-realm", a.getRealm());
			}
		} finally {
			server.stop();
		}
		
	}
	
}
