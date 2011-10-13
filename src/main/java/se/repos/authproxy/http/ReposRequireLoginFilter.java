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

import se.repos.authproxy.AuthFailedException;
import se.repos.authproxy.AuthRequiredException;
import se.repos.restclient.HttpStatusError;

/**
 * Simple filter that requires BASIC authentication for all requests.
 * Used as an alternative filter where the try-retry approach of
 * {@link ReposLoginOnDemandFilter} is not practical.
 * <p>
 * This filter must be configured with a realm, unlike on-demand authentication.
 * <p>
 * The filter interprets all {@link AuthFailedException} and
 * {@link HttpStatusError} with status 401 from the request handling chain
 * as failed authentication from the backend, leading to the
 * 401 Authentication Required message being sent again.
 * <p>
 * {@link AuthRequiredException} is treated as illegal state, because
 * as this filter never allows request handling without credentials,
 * it would be erroneous behavior from the servlet to throw that error.
 * <p>
 * TODO
 * It could provide different message bodies or additional headers
 * to distinguish between retry because of no authentication present
 * or retry because of failure from backend, but that would
 * be a deviation from what most HTTP servers do.
 */
public class ReposRequireLoginFilter implements Filter {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private String realm = "_realm_not_set_";
	
	void setRealm(String realm) {
		this.realm = realm;
	}
	
	String getRealm() {
		return this.realm;
	}
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		String realm = filterConfig.getInitParameter("realm");
		if (realm == null || realm.length() == 0) {
			throw new ServletException("Require Login filter needs a 'realm' init parameter. Use Login On Demand filter to autodetect realm.");
		}
		setRealm(realm);
		logger.info("Require Login filter was initialized with realm {}", getRealm());
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		logger.debug("Authentication filter running");
		HttpServletRequest httprequest = (HttpServletRequest) request;
		Principal user = httprequest.getUserPrincipal();

		if (user == null) {
			logger.debug("No user principal found, asking for retry with BASIC authentication");
			HttpServletResponse httpresponse = (HttpServletResponse) response;
			
			httpresponse.setHeader("WWW-Authenticate", "Basic realm=\"" + getRealm() + "\"");
			httpresponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		} else {
			logger.debug("Request authenticated as user {}", user);
			chain.doFilter(request, response);
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
