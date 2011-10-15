package se.repos.authproxy.restclient;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Test;

import se.repos.authproxy.ReposCurrentUser;

public class RestAuthenticationAuthproxyTest {

	@Test
	public void test() {
		ReposCurrentUser user = mock(ReposCurrentUser.class);
		when(user.getUsername()).thenReturn("us");
		when(user.getPassword()).thenReturn("pw");
		RestAuthenticationAuthproxy auth = new RestAuthenticationAuthproxy(user);
		assertEquals("us", auth.getUsername("", "", ""));
		assertEquals("pw", auth.getPassword("", "", "", "us"));
		assertNull(auth.getSSLSocketFactory(""));
	}

}
