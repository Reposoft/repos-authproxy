package se.repos.authproxy.http;

import javax.servlet.http.HttpServletRequest;

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
		String decoded = base64decode(encoded);
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
			holder.success(pair[0], pair[1]);
		}
		return this;
	}
	
	static String base64decode(String encoded) {
		try {
			return new String(javax.xml.bind.DatatypeConverter.parseBase64Binary(encoded));
		} catch (Exception e) {
			// continue
		}
//		try {
//			Class dc = Class.forName("sun.misc.BASE64Decoder");
//			if (Class.forName("sun.misc.BASE64Decoder") != null) {
//				return new String(sun.misc.BASE64Decoder().decodeBuffer(encoded));
//			}
//		} catch (Exception e) {
//			// continue
//		}
		// We'll probably end up here in java < 1.6
		try {
			if (Class.forName("org.apache.commons.codec.binary.Base64") != null) {
				return new String(org.apache.commons.codec.binary.Base64.decodeBase64(encoded));
			}
		} catch (Exception e2) {
			// continue
		}
		throw new RuntimeException("Failed to find a base64 decoder. Try to add commons-codec lib.");
	}
	
}
