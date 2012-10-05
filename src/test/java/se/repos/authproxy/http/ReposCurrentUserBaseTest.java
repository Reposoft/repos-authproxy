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

import static org.junit.Assert.*;

import org.junit.Test;

import se.repos.authproxy.AuthFailedException;
import se.repos.authproxy.AuthRequiredException;

public class ReposCurrentUserBaseTest {

	@Test
	public void test() {
	
		ReposCurrentUserBase b = new ReposCurrentUserBase() {
			private String u = null;
			private String p = null;
			@Override public String getUsername() { return u; }
			@Override public String getPassword() { return p; }				
			@Override void provide(String username, String password) {
				this.u = username;
				this.p = password;
			}
			@Override
			void clear() {
				this.u = null;
				this.p = null;
			}
		};
		
		assertFalse(b.isAuthenticated());
		try {
			b.getUsernameRequired("A realm");
			fail("Should have thrown AuthRequiredException");
		} catch (AuthRequiredException e) {
			assertEquals("A realm", e.getRealm());
		}

		try {
			b.getUsernameRequired(null);
			fail("Should throw AuthRequiredException without realm");
		} catch (AuthRequiredException e) {
			assertEquals(null, e.getRealm());
		}		
		
		try {
			b.setFailed("A realm");
			fail("Should not allow setFailed unless authenticated");
			// Services that want to skip this validation should just throw AuthFailedException
		} catch (IllegalStateException e) {
			assertEquals("Service reported authentication failure when not authenticated", e.getMessage());
		}
		
		b.provide("user", "pass");
		assertEquals("user", b.getUsernameRequired("anything, we don't support validation of realm after auth"));
		try {
			b.setFailed("A realm");
			fail("Should have thrown AuthFailedException at setFailed regardless of credentials");
		} catch (AuthFailedException e) {
			assertEquals("A realm", e.getRealm());
		}
	}

}
