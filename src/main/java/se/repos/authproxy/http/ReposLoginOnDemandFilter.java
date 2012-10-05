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

import se.repos.authproxy.AuthRequiredException;

/**
 * Catches {@link AuthRequiredException} and requests authentication
 * from client, leading to a new request with ReposAuthHolder populated.
 * <p>
 * If {@link AuthRequiredException} can not produce a realm for all services,
 * use instead {@link ReposLoginOnDemandRealmFilter} with fixed realm.
 * <p>
 * Web browsers usually remember the paths where credentials should be sent
 * with the requests so the try-retry roundtrip is normally done only once
 * per service.
 */
public class ReposLoginOnDemandFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Method not implemented");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Method not implemented");
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Method not implemented");
	}

}
