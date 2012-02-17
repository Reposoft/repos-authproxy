package se.repos.authproxy.http;

import javax.servlet.http.HttpServletRequest;

import se.repos.restclient.base.Codecs;
import se.repos.restclient.javase.RestClientJavaNet;

class BasicAuthToken {

	static final String HEADER_NAME = RestClientJavaNet.AUTH_HEADER_NAME; 
	static final String HEADER_PREFIX = RestClientJavaNet.AUTH_HEADER_PREFIX;
	
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
