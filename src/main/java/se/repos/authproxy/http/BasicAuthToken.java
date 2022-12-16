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

import javax.servlet.http.HttpServletRequest;

import se.repos.restclient.base.Codecs;

class BasicAuthToken {

	static final String HEADER_NAME = "Authorization"; 
	static final String HEADER_PREFIX = "Basic ";
	
	private String[] pair = null;
	
	BasicAuthToken(HttpServletRequest request) {
		String a = request.getHeader(HEADER_NAME);
		if (a == null) {
			return;
		}
		if (!a.startsWith(HEADER_PREFIX)) {
			throw new IllegalArgumentException(HEADER_NAME + " header value does not start with " + HEADER_PREFIX);
		}
		String encoded = a.substring(HEADER_PREFIX.length());
		String decoded = Codecs.base64decode(encoded);
		if (decoded == null || decoded.length() == 0) {
			throw new RuntimeException("Failed to decode base64 value for " + HEADER_NAME + " header");
		}
		this.pair = new String(decoded).split("\\:", 2);
		if (pair.length != 2) {
			throw new IllegalArgumentException(HEADER_NAME + " value does not seem to be a username password pair");
		}
	}
	
	boolean isFound() {
		return pair != null;
	}
	
	/**
	 * Stores credentials if found.
	 * @param holder To set credentials, done only if found
	 * @return the instance for chaining
	 */
	BasicAuthToken onto(ReposCurrentUserBase holder) {
		if (isFound()) {
			holder.provide(pair[0], pair[1]);
		}
		return this;
	}
	
}
