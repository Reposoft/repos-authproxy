package se.repos.authproxy.restclient;

import javax.inject.Inject;
import javax.net.ssl.SSLContext;

import se.repos.authproxy.AuthDetection;
import se.repos.authproxy.ReposCurrentUser;
import se.repos.restclient.RestAuthentication;

/**
 * Uses repos-authproxy to implement restclient's
 * Authentication credentials provider service.
 * <p>
 * As {@link #getUsername(String, String, String)} returns null if no
 * authentication has been made, this implementation is only suitable
 * together with {@link se.repos.authproxy.http.ReposRequireLoginFilter}
 * or exceptions understood in {@link AuthDetection}
 * or custom mapping of connection error to {@link se.repos.authproxy.AuthFailedException}.
 * <p>
 * Unlike {@link RestAuthenticationAuthproxy} this impl
 * can be used to check if a user is authenticated.
 * Normally, however, this should be done through {@link ReposCurrentUser}.
 * <p>
 * Nothing is cached here; Every call to this class produces a
 * new call to {@link ReposCurrentUser},
 * allowing thread-safe operation with different credentials in each thread
 * where the {@link ReposCurrentUser} impl operates like that.
 */
public class RestAuthenticationAuthproxyNorequire implements RestAuthentication {

	private ReposCurrentUser user;

	/**
	 * Uses default {@link ReposCurrentUser}.
	 */
	public RestAuthenticationAuthproxyNorequire() {
		this(ReposCurrentUser.DEFAULT);
	}
	
	/**
	 * Custom credentials management.
	 */
	@Inject
	public RestAuthenticationAuthproxyNorequire(ReposCurrentUser reposCurrentUser) {
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
