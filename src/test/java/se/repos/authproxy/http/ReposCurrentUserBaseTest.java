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
