/**
 * Copyright (C) 2004-2012 Repos Mjukvara AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.repos.authproxy.restclient;

import javax.inject.Inject;
import javax.net.ssl.SSLContext;

import se.repos.authproxy.AuthDetection;
import se.repos.authproxy.ReposCurrentUser;
import se.repos.restclient.RestAuthentication;

/**
 * Crendentials provider to as-needed BASIC authentication
 * in repos-restclient, backed by authproxy.
 * <p>
 * Enforces authentication and can therefore not be used to 
 * see if there is user authentication or not.
 * Any call to {@link #getUsername(String, String, String)}
 * causes a {@link ReposCurrentUser#getUsernameRequired(String)}.
 * {@link RestAuthenticationAuthproxyNorequire} can be used for
 * checks without requiring authentication, but with it the authproxy
 * concept requires all exceptions resulting from authentication
 * not set to be recognized in {@link AuthDetection}.
 * {@link ReposCurrentUser} is the authproxy compatible way
 * of checking for authentication.
 * <p>
 * Nothing is cached here; Every call to this class produces a
 * new call to {@link ReposCurrentUser},
 * allowing thread-safe operation with different credentials in each thread
 * where the {@link ReposCurrentUser} impl operates like that.
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
		if (realm == null) {
			throw new IllegalArgumentException("Realm detection is required for authproxy REST authentication");
		}
		return user.getUsernameRequired(realm);
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
