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
package se.repos.authproxy.http;

import se.repos.authproxy.AuthFailedException;
import se.repos.authproxy.AuthRequiredException;
import se.repos.authproxy.ReposCurrentUser;

/**
 * The logic that is not related to storage.
 */
public abstract class ReposCurrentUserBase implements ReposCurrentUser {

	/**
	 * Called when credentials are provided.
	 * This being authproxy, credentials are not validated at this point.
	 * @param username Provided login name
	 * @param password Provided login passowrd
	 */
	abstract void provide(String username, String password);
	
	/**
	 * Called after processing to clear credentials,
	 * essential for storage in thread local etc.
	 */
	abstract void clear();
	
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
