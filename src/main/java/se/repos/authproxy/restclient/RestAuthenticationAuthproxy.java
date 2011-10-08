package se.repos.authproxy.restclient;

import javax.inject.Inject;
import javax.net.ssl.SSLSocketFactory;

import se.repos.authproxy.ReposCurrentUser;
import se.repos.restclient.RestAuthentication;

/**
 * Uses repos-authproxy to implement restclient's Authentication
 * credentials provider service.
 */
public class RestAuthenticationAuthproxy implements RestAuthentication {

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
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Method not implemented");		
	}
	
	@Override
	public String getUsername(String root, String resource, String realm) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Method not implemented");
	}

	@Override
	public String getPassword(String root, String resource, String realm,
			String username) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Method not implemented");
	}

	/**
	 * It is unlikely that this metod can be implemented for authproxy based credentials,
	 * as SSL cert authentication can not be forwarded.
	 */
	@Override
	public SSLSocketFactory getSSLSocketFactory(String root) {
		throw new UnsupportedOperationException("SSL cert authentication not supported by Authproxy");
	}

}
