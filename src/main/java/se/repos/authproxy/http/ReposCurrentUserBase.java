package se.repos.authproxy.http;

import se.repos.authproxy.AuthFailedException;
import se.repos.authproxy.AuthRequiredException;
import se.repos.authproxy.ReposCurrentUser;

/**
 * The logic that is not related to storage.
 */
public abstract class ReposCurrentUserBase implements ReposCurrentUser {

	abstract void success(String username, String password);
	
	@Override
	public boolean isAuthenticated() {
		return getUsername() != null;
	}
	
	@Override
	public String getUsernameRequired(String realm) throws AuthRequiredException {
		if (!isAuthenticated()) {
			throw new AuthRequiredException("", realm);
		}
		return getUsername();
	}

	@Override
	public void setFailed(String realm) throws AuthFailedException {
		if (!isAuthenticated()) {
			throw new IllegalStateException("Service reported authentication failure when not authenticated");
		}
		throw new AuthFailedException("", realm);
	}

}
