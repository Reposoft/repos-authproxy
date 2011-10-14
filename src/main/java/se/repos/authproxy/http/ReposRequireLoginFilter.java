package se.repos.authproxy.http;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

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
import se.repos.authproxy.ReposCurrentUser;
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
	
	private ReposCurrentUserBase currentUser;
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
		currentUser = (ReposCurrentUserBase) ReposCurrentUser.DEFAULT; // could use an init param to set custom, unless we have dependency injection in filters 
		logger.info("Require Login filter initialized with realm {}, holder {}", getRealm(), currentUser);
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse resp = (HttpServletResponse) response;
		logger.debug("Authentication filter invoked");

		if (!new BasicAuthToken(req).onto(currentUser).isFound()) {
			requireAuthentication(resp, getRealm());
			return; // proceeding with chain would lead to illegal state
		}

		logger.debug("The request is authenticated as user '{}'", currentUser.getUsername());
		try {
			chain.doFilter(request, response);
		} catch (AuthFailedException e) {
			// TODO make sure body output has not started
			logger.info("Authentication failure from service detected.", e);
			requireAuthentication(resp, getRealm());
			return;
		} catch (HttpStatusError h) {
			// TODO make sure body output has not started
			logger.info("REST authentication failure from service detected.", h);
			if (h.getHttpStatus() == 401) {
				List<String> header = h.getHeaders().get("WWW-Authenticate");
				if (header != null && header.size() > 0) {
					requireAuthentication(resp, getRealm());
					return;
				} else {
					// Warn because this would not work with on-demand authentication
					logger.error("Auth proxy received 401 status without authentication header");
					throw h;
				}
			} else {
				throw h;
			}
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

	/**
	 * Send response to require authentication, using default response body.
	 */
	private void requireAuthentication(HttpServletResponse resp, String realm) throws IOException {
		resp.setHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
		resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
	}

	@Override
	public void destroy() {
		logger.debug("Filter destroyed");
	}

}
