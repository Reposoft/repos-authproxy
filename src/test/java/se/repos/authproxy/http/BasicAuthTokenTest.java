package se.repos.authproxy.http;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;

public class BasicAuthTokenTest {

	@Test
	public void testHaveCredentials() {
		HttpServletRequest req = mock(HttpServletRequest.class);
		when(req.getHeader("Authorization")).thenReturn("Basic YXBhOmJlcGE=");
		ReposCurrentUserBase holder = mock(ReposCurrentUserBase.class);
		assertTrue("Should consider us authenticated",
				new BasicAuthToken(req).onto(holder).isFound());
		verify(holder).provide("apa", "bepa");
	}

	@Test
	public void testHaveNot() {
		HttpServletRequest req = mock(HttpServletRequest.class);
		when(req.getHeader("Authorization")).thenReturn(null);
		ReposCurrentUserBase holder = mock(ReposCurrentUserBase.class);
		assertFalse("Should not be authenticated",
				new BasicAuthToken(req).onto(holder).isFound());
		verify(holder, times(0)).provide(anyString(), anyString());
	}

	@Test
	public void testNotBasic() {
		HttpServletRequest req = mock(HttpServletRequest.class);
		when(req.getHeader("Authorization")).thenReturn("NotBasic YXBhOmJlcGE=");
		try {
			new BasicAuthToken(req);
			fail("Should have thrown exception when authorization header is not BASIC");
		} catch (Exception e) {
			assertFalse("Do not reveal passwords in logs", e.getMessage().contains("YXBhOmJlcGE="));
			assertEquals("Authorization header value does not start with Basic ", e.getMessage());
		}
	}
	
}
