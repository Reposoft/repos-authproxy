package se.repos.authproxy;

/**
 * In the HTTP BASIC model this could actually mean the same as 
 * {@link AuthFailedException}, but can not be thrown if
 * credentials are already provided by the proxy.
 */
public class AuthRequiredException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private String realm;

	/**
	 * 
	 * @param realm realm name for login, usually from WWW-Authenticate header value
	 */
	public AuthRequiredException(String message, String realm) {
		super(message);
		this.realm = realm;
	}

	public AuthRequiredException(Throwable cause, String realm) {
		super(cause);
		this.realm = realm;
	}
	
	public String getRealm() {
		return this.realm;
	}
	
}
