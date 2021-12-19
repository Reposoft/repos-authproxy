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
 * Simple filter that requires X-Forwarded-User header for all requests.
 * TODO: Password empty or bogus?
 */
public class ReposRequireRemoteuserFilter implements Filter {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private ReposCurrentUserBase currentUser;
	private AuthDetection authDetection = AuthDetection.all; // TODO activate known
	
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		currentUser = (ReposCurrentUserBase) ReposCurrentUser.DEFAULT; // could use an init param to set custom, unless we have dependency injection in filters 
		logger.info("Require Login filter initialized, holder {}", currentUser);
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse resp = (HttpServletResponse) response;

		// This filter always requires a logged in username via header.
		String userHeader = req.getHeader("X-Forwarded-User");
		if (userHeader == null || userHeader.trim().isEmpty()) {
			logger.debug("Requesting authentication");
			requireAuthentication(resp);
			return; // proceeding with chain would lead to illegal state
		} else {
			currentUser.provide(userHeader, "");
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
			logger.warn("Authentication failure from service detected (likely authz).", e);
			requireAuthentication(resp);
			return;
		}
	}

	/**
	 * Send response to require authentication, using default response body.
	 */
	private void requireAuthentication(HttpServletResponse resp) throws IOException {
		//resp.setHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
		resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
	}

	@Override
	public void destroy() {
		logger.debug("Filter destroyed");
	}

}
