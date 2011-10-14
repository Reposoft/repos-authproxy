package se.repos.authproxy;

/**
 * Thrown by any backend to signal that the credentials provided
 * by the auth proxy was rejected at authentication,
 * and that retry should be attempted.
 * <p>
 * The auth proxy also understands {@link se.repos.restclient.HttpStatusError} with status 401.
 * <p>
 * See also {@link AuthRequiredException}, but consider using
 * {@link ReposCurrentUser#getUsernameRequired(String)} and
 * {@link ReposCurrentUser#setFailed(String)} instead.
 * <p>
 * Note that in HTTP auth there is no difference between missing and invalid login;
 * the server will keep sending 401 Authentication Required for every {@link AuthFailedException}.
 */
public class AuthFailedException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private String realm;

	/**
	 * Signal failed or missing authentication without a realm name,
	 * WARNING this will make it impossible for a stateless HTTP
	 * BASIC auth impl to request authentication, unless there is a
	 * preconfigured realm.
	 */
	public AuthFailedException(String message) {
		super(message);
	}

	/**
	 * Signal failed or missing authentication without a realm name,
	 * WARNING this will make it impossible for a stateless HTTP
	 * BASIC auth impl to request authentication, unless there is a
	 * preconfigured realm.
	 */	
	public AuthFailedException(Throwable cause) {
		super(cause);
	}

	/**
	 * Signal failed or missing authentication with realm.
	 * @param message Optional message that the auth impl might
	 *  be capable of forwarding to the user
	 * @param realm The authentication realm
	 */
	public AuthFailedException(String message, String realm) {
		this(message);
		this.realm = realm;
	}

	/**
	 * @param cause The actual authentication error
	 * @param realm The authentication realm
	 */
	public AuthFailedException(Throwable cause, String realm) {
		this(cause);
		this.realm = realm;
	}
	
	/**
	 * @return realm realm name for login, for WWW-Authenticate header value
	 */
	public String getRealm() {
		return this.realm;
	}
	
}
