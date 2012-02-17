package se.repos.authproxy.restclient;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Test;

import se.repos.authproxy.ReposCurrentUser;

public class RestAuthenticationAuthproxyTest {

	@Test
	public void test() {
		ReposCurrentUser user = mock(ReposCurrentUser.class);
		when(user.getUsername()).thenReturn("us").thenReturn("us2");
		when(user.getPassword()).thenReturn("pw").thenReturn("pw2");
		RestAuthenticationAuthproxy auth = new RestAuthenticationAuthproxy(user);
		assertEquals("us", auth.getUsername("", "", ""));
		assertEquals("pw", auth.getPassword("", "", "", "us"));
		assertNull(auth.getSSLContext(""));
		assertEquals("should allow backend to provide different credentials each time or in each thread",
				"us2", auth.getUsername("", "", ""));
		assertEquals("pw2", auth.getPassword("", "", "", "us2"));
	}

}
