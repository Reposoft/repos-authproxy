package se.repos.authproxy.http;

import se.repos.authproxy.ReposCurrentUser;

/**
 * Like Spring Security does it.
 */
public class ReposCurrentUserThreadLocal implements ReposCurrentUser {

	// TODO how to set in a protected manner?
	
	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Method not implemented");
	}
	
	@Override
	public String getRealm() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Method not implemented");
	}
	
	@Override
	public String getPassword() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Method not implemented");
	}

	@Override
	public boolean isAuthenticated() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Method not implemented");
	}

	@Override
	public String getUsernameRequired(String realm) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Method not implemented");
	}

}
