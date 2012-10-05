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

/**
 * The preferred way to signal that authentication was required
 * by a service, with known realm name.
 * <p>
 * Consider using {@link ReposCurrentUser#getUsernameRequired(String)}
 * to throw this.
 * <p>
 * Note that in HTTP auth there is no difference between missing and invalid login.
 * 
 */
public class AuthRequiredException extends AuthFailedException {

	private static final long serialVersionUID = 1L;

	public AuthRequiredException(String message, String realm) {
		super(message, realm);
	}

	public AuthRequiredException(Throwable cause, String realm) {
		super(cause, realm);
	}
	
}
