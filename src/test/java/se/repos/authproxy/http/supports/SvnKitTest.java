package se.repos.authproxy.http.supports;

import static org.junit.Assert.*;

import org.junit.Test;

import se.repos.authproxy.ReposAuthFailedException;

/**
 * Test that svn authentication and authorization errors
 * can be translated to {@link ReposAuthFailedException}
 * <em>with realm</em>.
 * If we can't get realm the authproxy would have to do
 * an extra HTTP request before it can ask for authentication.
 */
public class SvnKitTest {

	@Test
	public void testToReposAuthFailedException() {
		fail("Not yet implemented");
	}

	@Test
	public void testToReposAuthRequiredException() {
		fail("Not yet implemented");
	}
	
}
