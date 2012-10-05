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

/**
 * Inspired by Spring Security.
 * See http://static.springsource.org/spring-security/site/docs/3.1.x/apidocs/org/springframework/security/core/context/SecurityContextHolder.html
 * 
 * Compare
 * http://static.springsource.org/spring-security/site/xref/org/springframework/security/context/ThreadLocalSecurityContextHolderStrategy.html
 * with
 * http://static.springsource.org/spring-security/site/xref/org/springframework/security/context/GlobalSecurityContextHolderStrategy.html
 */
public class ReposCurrentUserThreadLocal extends ReposCurrentUserBase {

	private static ThreadLocal<String> u = new ThreadLocal<String>();
	private static ThreadLocal<String> p = new ThreadLocal<String>();
	
	@Override
	public String getUsername() {
		return u.get();
	}
	
	@Override
	public String getPassword() {
		return p.get();
	}

	@Override
	void provide(String username, String password) {
		u.set(username);
		p.set(password);
	}

	@Override
	void clear() {
		u.set(null);
		p.set(null);
	}

}
