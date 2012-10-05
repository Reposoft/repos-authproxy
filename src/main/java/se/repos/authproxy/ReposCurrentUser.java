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
package se.repos.authproxy;

import se.repos.authproxy.http.ReposCurrentUserThreadLocal;

/**
 * Provides username and password along with methods
 * to signal authentication prompt and failure.
 * 
 * <p>
 * Service that knows authentication is required does:
 * <ul>
 * <li>{@link #getUsernameRequired(String)}
 * <li>{@link #getPassword()}
 * <li>Validate credentials
 * <li>On rejection {@link #setFailed(String)}
 * </ul>
 * 
 * <p>
 * Service that prompts for authentication when needed does:
 * <ul>
 * <li>{@link #getUsername()} and {@link #getPassword()} if {@link #isAuthenticated()}
 * <li>Make service call, catch authentication exception
 * <li>On authentication error, {@link #setFailed(String)}.
 * <li>(could probably do {@link #getUsernameRequired(String)} instead,
 * hoping that the current authentication model can prompt at this stage
 * </ul>
 * 
 * <p>
 * Alternative for service that uses HTTP calls to its backend:
 * <ul>
 * <li>Configure HTTP lib to {@link #getUsername()} and {@link #getPassword()} if {@link #isAuthenticated()} 
 * <li>Make service call, catch status 401
 * <li>Preferrably extract Realm name from the status error
 * <li>On authentication error (i.e. including rejected credentials), throw {@link AuthFailedException}
 * </ul>
 * 
 * Throwing {@link AuthFailedException} has the advantage of not needing
 * access to this interface, but on the other hand authentication should
 * ideally be transparent to the business logic; for example by configuring
 * both retrieval of credentials and detection of authentication failures
 * when setting up the libraries used by the service.
 * 
 * <p>
 * Instances of this interface may be shared in an application but
 * must always produce the credentials of the current user,
 * for example in a single HTTP request or any kind of session.
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
	 * <p>
	 * (Maybe this method is not as useful as it seemed when the interface was designed,
	 * because the main use case for this library is that you don't know
	 * realm until after authentication failure, at which point you'll
	 * probably just call {@link #setFailed(String)}.)
	 * <p>
	 * Corresponds to a BASIC auth prompt in a browser,
	 * but HTTP auth means that the request is retried.
	 * This method represents a generic authentication model
	 * where you first ask for credentials and then validate them.
	 * <p>
	 * Upon {@link AuthRequiredException}, when the caller retries
	 * with credentials, this method will return that username,
	 * expecting the service to call {@link #setFailed(String)} if
	 * authentication proves invalid.
	 * <p>
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
