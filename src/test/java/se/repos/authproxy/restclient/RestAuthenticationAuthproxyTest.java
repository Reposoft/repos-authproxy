package se.repos.authproxy.restclient;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Test;

import se.repos.authproxy.ReposCurrentUser;

public class RestAuthenticationAuthproxyTest {

	@Test
	public void testNorequire() {
		ReposCurrentUser user = mock(ReposCurrentUser.class);
		when(user.getUsername()).thenReturn("us").thenReturn("us2");
		when(user.getPassword()).thenReturn("pw").thenReturn("pw2");
		RestAuthenticationAuthproxyNorequire auth = new RestAuthenticationAuthproxyNorequire(user);
		assertEquals("us", auth.getUsername("", "", ""));
		assertEquals("pw", auth.getPassword("", "", "", "us"));
		assertNull(auth.getSSLContext(""));
		assertEquals("should allow backend to provide different credentials each time or in each thread",
				"us2", auth.getUsername("", "", ""));
		assertEquals("pw2", auth.getPassword("", "", "", "us2"));
	}

	@Test
	public void testNotAuthenticated() {
		ReposCurrentUser user = mock(ReposCurrentUser.class);
		RestAuthenticationAuthproxy auth = new RestAuthenticationAuthproxy(user);
		auth.getUsername(null, null, "realmx");
		verify(user).getUsernameRequired("realmx"); // used by default impl to produce an authproxy exception containing auth challenge
		verifyNoMoreInteractions(user);
	}

	@Test
	public void testAuthenticated() {
		ReposCurrentUser user = mock(ReposCurrentUser.class);
		// can not validate realm, but use it to make sure that credentials are not cached 
		when(user.getUsernameRequired("x")).thenReturn("usx");
		when(user.getUsernameRequired("y")).thenReturn("usy");
		when(user.getPassword()).thenReturn("pw").thenReturn("pw2");
		
		RestAuthenticationAuthproxy auth = new RestAuthenticationAuthproxy(user);
		assertEquals("usx", auth.getUsername(null, null, "x"));
		assertEquals("pw", auth.getPassword(null, null, "x", "usx"));
		assertEquals("usy", auth.getUsername(null, null, "y"));
		assertEquals("pw2", auth.getPassword(null, null, "x", "usy"));
	}
	
	// This is important to enforce that getUsername is only used when authentication is required
	@Test(expected=IllegalArgumentException.class)
	public void testNorealm() {
		new RestAuthenticationAuthproxy(mock(ReposCurrentUser.class))
			.getUsername("http://localhost", "/", null);
	}
	
}
