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
		cu.provide("me", "my");
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
				cu.provide("t1", "pwd1");
			}
		});
		final Thread t2 = new Thread(new Runnable() {
			@Override public void run() {
				assertFalse("Should not be authenticated yet in thread " 
						+ Thread.currentThread().getId(), cu.isAuthenticated());
				cu.provide("t2", "pwd2");
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
				cu.provide("t1", "pwd1");
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
				cu.provide("t2", "pwd2");
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
	
	@Test
	public void testClear() {
		ReposCurrentUserThreadLocal cu = new ReposCurrentUserThreadLocal();
		cu.provide("u", "p");
		assertEquals("u", cu.getUsername());
		assertEquals("p", cu.getPassword());
		assertTrue(cu.isAuthenticated());
		cu.clear();
		assertEquals(null, cu.getUsername());
		assertEquals(null, cu.getPassword());
		assertFalse(cu.isAuthenticated());
	}
	
}
