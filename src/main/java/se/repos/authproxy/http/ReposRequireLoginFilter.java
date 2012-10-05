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

import java.io.IOException;

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

import se.repos.authproxy.AuthDetection;
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
	private AuthDetection authDetection = AuthDetection.all; // TODO activate known
	
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

		// This filter always requires login, although it can not validate them other than through exceptions received
		if (!new BasicAuthToken(req).onto(currentUser).isFound()) {
			logger.debug("Requesting authentication");
			requireAuthentication(resp, getRealm());
			return; // proceeding with chain would lead to illegal state
		}

		logger.debug("The request is authenticated as user '{}'", currentUser.getUsername());
		try {
			try {
				chain.doFilter(request, response);
			} catch (IOException e) {
				authDetection.analyze(e);
				throw e;
			} catch (ServletException e) {
				authDetection.analyze(e);
				throw e;
			} catch (RuntimeException e) {
				authDetection.analyze(e);
				throw e;
			} finally {
				// This is not strictly necessary when login is required, but should be done in all filters anyway
				currentUser.clear();
			}
		} catch (AuthFailedException e) {
			// TODO make sure body output has not started
			logger.info("Authentication failure from service detected.", e);
			if (e.getRealm() == null || e.getRealm().length() == 0) {
				logger.warn("No login realm provided for auth exception -- incompatible with on-demand auth");
			}
			requireAuthentication(resp, getRealm());
			return;
		}
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
