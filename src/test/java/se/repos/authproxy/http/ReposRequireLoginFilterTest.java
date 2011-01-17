package se.repos.authproxy.http;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.Filter;

import org.eclipse.jetty.embedded.HelloServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.Test;

import se.repos.restclient.HttpStatusError;
import se.repos.restclient.RestResponseBean;
import se.repos.restclient.javase.RestGetClientJavaNet;

public class ReposRequireLoginFilterTest {

	@Test
	public void testDoFilter() {
		fail("Not yet implemented");
	}
	
	@Test
	public void testFilterJetty() throws Exception {
		// http://wiki.eclipse.org/Jetty/Tutorial/Embedding_Jetty
		
		int port = 49999; // TODO random test port
        Server server = new Server(port);
 
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
 
        context.addServlet(new ServletHolder(new HelloServlet()),"/*");
        Filter filter = new ReposRequireLoginFilter();
        context.addFilter(new FilterHolder(filter), "/*", 0);

        server.start();
        
        try {
        	String url = "http://localhost:" + port + "/";
        	try {
        		new RestGetClientJavaNet().get(url, new RestResponseBean());
        		fail("Request should be interceptet by filter that requires authentication");
        	} catch (HttpStatusError e) {
        		assertEquals(401, e.getHttpStatus());
        	}
        	
        } finally {
        	server.stop();
        }
	}

}
