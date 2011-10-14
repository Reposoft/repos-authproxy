package se.repos.authproxy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	
	/**
	 * Analyze an exception to look for authentication errors from
	 * a bunch of known libraries: svnkit, solrj.
	 * See tests in "supports" package.
	 * @param e Any exception
	 * @throws AuthFailedException If the exception is a known authentication failure
	 */
	public static void analyze(Exception e) throws AuthFailedException {
		analyzeSvnKit(e);
	}
	
	private static void analyzeSvnKit(Exception e) {
		if ("org.tmatesoft.svn.core.SVNAuthenticationException".equals(e.getClass().getName())) {
			// svn: Authentication required for '<http://localhost:49999> See-if-svnkit-detects-this-realm
			Matcher matcher = Pattern.compile(".*Authentication required for '\\S+ (.*)'").matcher(e.getMessage());
			if (matcher.matches()) {
				throw new AuthFailedException(e, matcher.group(1));
			}
		}
	}
	
}
