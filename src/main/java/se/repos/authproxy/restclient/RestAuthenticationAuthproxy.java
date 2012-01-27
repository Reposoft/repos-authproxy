package se.repos.authproxy.restclient;

import javax.inject.Inject;
import javax.net.ssl.SSLContext;

import se.repos.authproxy.ReposCurrentUser;
import se.repos.restclient.RestAuthentication;

/**
 * Uses repos-authproxy to implement restclient's Authentication
 * credentials provider service.
 */
public class RestAuthenticationAuthproxy implements RestAuthentication {

	private ReposCurrentUser user;

	/**
	 * Uses default {@link ReposCurrentUser}.
	 */
	public RestAuthenticationAuthproxy() {
		this(ReposCurrentUser.DEFAULT);
	}
	
	/**
	 * Custom credentials management.
	 */
	@Inject
	public RestAuthenticationAuthproxy(ReposCurrentUser reposCurrentUser) {
		this.user = reposCurrentUser;	
	}
	
	@Override
	public String getUsername(String root, String resource, String realm) {
		return user.getUsername();
	}

	@Override
	public String getPassword(String root, String resource, String realm,
			String username) {
		return user.getPassword();
	}

	/**
	 * It is unlikely that this metod can be implemented for authproxy based credentials,
	 * as SSL cert authentication can not be forwarded.
	 */
	@Override
	public SSLContext getSSLContext(String root) {
		return null;
	}

}
