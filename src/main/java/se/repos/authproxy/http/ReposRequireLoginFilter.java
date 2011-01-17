package se.repos.authproxy.http;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Experimental filter, requiring authentication until
 * we have a solution for on-demand with realm.
 */
public class ReposRequireLoginFilter implements Filter {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * Until we have a forward-auth-when-backend-requires-it solution,
	 * like in Repos Web, realm must be hard coded (or in servlet context).
	 */
	public static final String AUTH_REALM = "Repos";
	
	private String getRealm() {
		return AUTH_REALM;
	}
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		logger.debug("Authentication filter running");
		HttpServletRequest httprequest = (HttpServletRequest) request;
		Principal user = httprequest.getUserPrincipal();

		if (user == null) {
			HttpServletResponse httpresponse = (HttpServletResponse) response;
			
			httpresponse.setHeader("WWW-Authenticate", "Basic realm=\"" + getRealm() + "\"");
			httpresponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		}
		/* old code that assumes auth configured in tomcat
		if (user != null) {
			String strUser = user.getName();
			if (strUser != null && strUser.length() > 0) {
				CurrentUser.setUsername(strUser);
			} else {
				logger.error("Username is empty");
			}
		}
		
		// Continue processing the rest of the filter chain.
		try {
		*/
			chain.doFilter(request, response);
		/*
		} finally {
			// Remove the added element again - only if added.
			CurrentUser.clearUsername();
		}
		*/
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

}
