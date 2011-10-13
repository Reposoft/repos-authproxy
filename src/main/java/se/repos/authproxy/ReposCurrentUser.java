package se.repos.authproxy;

import se.repos.authproxy.http.ReposCurrentUserThreadLocal;

/**
 * Instances may be shared in an application but must always produce
 * the credentials of the current user, for example in a single
 * HTTP request or any kind of session.
 */
public interface ReposCurrentUser {

	/**
	 * Used where there's no dependency injection or custom filter config.
	 */
	public ReposCurrentUser DEFAULT = new ReposCurrentUserThreadLocal();
	
	/**
	 * In some scenarios authentication is only sent to the subsystem if
	 * set in the originating request.
	 */
	public boolean isAuthenticated();
	
	/**
	 * HTTP authentication concept.
	 * @return the realm name for which the username and password was entered
	 */
	public String getRealm();
	
	public String getUsername();

	/**
	 * Gets the username if set AND if the provided realm is identical
	 * to that for the current user, otherwise throws an
	 * {@link AuthRequiredException} with the realm.
	 * @param realm For matching and for requesting auth when needed.
	 * @return Username if authenticated with the given realm name
	 * @throws AuthRequiredException if no such login has been made
	 */
	public String getUsernameRequired(String realm) throws AuthRequiredException;
	
	public String getPassword();
	
}
