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
	
	public String getUsername();

	public String getPassword();
	
	/**
	 * Gets the username if set, otherwise throws an {@link AuthRequiredException}
	 * with the realm.
	 * 
	 * Although realm is not provided when sending the credentials in HTTP BASIC
	 * auth, some implementations might be able to validate that the given realm
	 * matches the one that the user authenticated for.
	 * 
	 * @param realm For requesting auth when needed.
	 * @return The authenticated username
	 * @throws AuthRequiredException if no login has been made
	 */
	public String getUsernameRequired(String realm) throws AuthRequiredException;
	
	/**
	 * Signal that the current credentials failed to authenticate the user in a service.
	 * @throws AuthFailedException always
	 */
	public void setFailed(String realm) throws AuthFailedException;
	
}
