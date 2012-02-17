package se.repos.authproxy;

/**
 * The preferred way to signal that authentication was required
 * by a service, with known realm name.
 * <p>
 * Consider using {@link ReposCurrentUser#getUsernameRequired(String)}
 * to throw this.
 * <p>
 * Note that in HTTP auth there is no difference between missing and invalid login.
 * 
 */
public class AuthRequiredException extends AuthFailedException {

	private static final long serialVersionUID = 1L;

	public AuthRequiredException(String message, String realm) {
		super(message, realm);
	}

	public AuthRequiredException(Throwable cause, String realm) {
		super(cause, realm);
	}
	
}
