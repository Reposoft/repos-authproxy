package se.repos.authproxy.http;

import static org.junit.Assert.*;

import org.junit.Test;

import se.repos.authproxy.AuthFailedException;
import se.repos.authproxy.AuthRequiredException;

public class ReposCurrentUserThreadLocalTest {

	@Test
	public void testSingleThread() {
		ReposCurrentUserBase cu = new ReposCurrentUserThreadLocal();
		assertFalse(cu.isAuthenticated());
		assertNull(cu.getUsername());
		assertNull(cu.getPassword());
		try {
			cu.getUsernameRequired("A realm");
			fail("Should have thrown auth signal");
		} catch (AuthRequiredException e) {
			assertEquals("A realm", e.getRealm());
		}
		try {
			cu.setFailed("B realm");
			fail("Should have thrown exception"); // is this just an annoying check?
		} catch (IllegalStateException e) {
			assertEquals("Service reported authentication failure when not authenticated", e.getMessage());
		}
		cu.success("me", "my");
		assertTrue(cu.isAuthenticated());
		assertEquals("me", cu.getUsername());
		assertEquals("my", cu.getPassword());
		assertEquals("me", cu.getUsernameRequired("Any realm now"));
		try {
			cu.getUsernameRequired("");
		} catch (IllegalArgumentException e) {
			assertEquals("Authentication Realm may not be an empty string", e.getMessage());
		}
		try {
			cu.setFailed("B realm");
			fail("Should have thrown AuthFailedException");
		} catch (AuthFailedException e) {
			assertEquals("B realm", e.getRealm());
			// expected
		}
	}

	@Test
	public void testMultipleThreadsSingleInstance() {
		// Note that this is the same thread as previous test so here auth is probably set
		final ReposCurrentUserBase cu = new ReposCurrentUserThreadLocal(); 
		final Thread t1 = new Thread(new Runnable() {
			@Override public void run() {
				assertFalse("Should not be authenticated yet in thread " 
						+ Thread.currentThread().getId(), cu.isAuthenticated());
				cu.success("t1", "pwd1");
			}
		});
		final Thread t2 = new Thread(new Runnable() {
			@Override public void run() {
				assertFalse("Should not be authenticated yet in thread " 
						+ Thread.currentThread().getId(), cu.isAuthenticated());
				cu.success("t2", "pwd2");
				t1.start();
				try {
					t1.join();
				} catch (InterruptedException e) {
					throw new RuntimeException("Error not handled", e);
				}
				assertEquals("thread " + Thread.currentThread().getId(), "t2", cu.getUsername());
			}
		});
		t2.start();
		try {
			t2.join();
		} catch (InterruptedException e) {
			throw new RuntimeException("Error not handled", e);
		}
	}

	@Test
	public void testMultipleThreadsSeparateInstances() {
		final Thread t1 = new Thread(new Runnable() {
			ReposCurrentUserBase cu = new ReposCurrentUserThreadLocal();
			@Override public void run() {
				cu.success("t1", "pwd1");
				assertEquals("t1", cu.getUsername());
			}
		});
		t1.start();
		final Thread t2 = new Thread(new Runnable() {
			ReposCurrentUserBase cu = new ReposCurrentUserThreadLocal();
			@Override public void run() {
				try {
					t1.join();
				} catch (InterruptedException e) {
					throw new RuntimeException("Error not handled", e);
				}
				assertFalse(cu.isAuthenticated());
				cu.success("t2", "pwd2");
				assertEquals("t2", cu.getUsername());
			}
		});
		t2.start();
		try {
			t2.join();
		} catch (InterruptedException e) {
			throw new RuntimeException("Error not handled", e);
		}
	}
	
}
