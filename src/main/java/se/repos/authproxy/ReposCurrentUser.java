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

	public String getUsernameRequired();
	
	public String getPassword();
	
}
