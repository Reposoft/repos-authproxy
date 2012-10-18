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
package se.repos.authproxy.transfer;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import se.repos.authproxy.ReposCurrentUser;
import se.repos.authproxy.http.ReposCurrentUserThreadLocal;

public class AuthproxyTrustedTransferInstanceShareTest {

	@Test
	public void test() throws Exception {
		final ReposCurrentUser u = new ReposCurrentUserThreadLocal();
		
		Method provide = ReposCurrentUserThreadLocal.class.getDeclaredMethod("provide", String.class, String.class);
		provide.setAccessible(true);
		provide.invoke(u, "username1", "password1");
		
		final AuthproxyTrustedTransfer transfer = new AuthproxyTrustedTransferInstanceShare(u);
		
		ExecutorService executor = Executors.newSingleThreadExecutor();

		Future<Boolean> task0 = executor.submit(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				return "username1".equals(u.getUsername()) && "password1".equals(u.getPassword());
			}
		});
		executor.awaitTermination(100, TimeUnit.MILLISECONDS);
		assertFalse("should have no auth when no transfer has been done", task0.get());		
		
		transfer.capture();
		Future<Boolean> task1 = executor.submit(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				transfer.spread();
				return "username1".equals(u.getUsername()) && "password1".equals(u.getPassword());
			}
		});
		executor.awaitTermination(100, TimeUnit.MILLISECONDS);
		assertTrue("should have auth when transfer is done", task1.get());		

		Future<Boolean> task1x = executor.submit(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				boolean got = "username1".equals(u.getUsername()) && "password1".equals(u.getPassword());
				transfer.undo();
				return got;
			}
		});
		executor.awaitTermination(100, TimeUnit.MILLISECONDS);
		assertTrue("in single thread mode we expect transfer to survive until undone", task1x.get());	
		
		Future<Boolean> task2 = executor.submit(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				return "username1".equals(u.getUsername()) && "password1".equals(u.getPassword());
			}
		});
		executor.awaitTermination(100, TimeUnit.MILLISECONDS);
		assertFalse("should have no auth when transfer has been undone", task2.get());
	}
	
	@Test
	public void testMustCapture() {
		ReposCurrentUserThreadLocal u = new ReposCurrentUserThreadLocal();
		AuthproxyTrustedTransfer transfer = new AuthproxyTrustedTransferInstanceShare(u);
		try {
			transfer.spread();
			fail("Should not allow spread unless captured");
		} catch (IllegalStateException e) {
			// expected
		}
		transfer.capture();
		try {
			transfer.capture();
			fail("Users of this service should really know what they're doing and another capture could indicate flaws");
		} catch (IllegalStateException e) {
			// expected
		}
		transfer.spread();
		try {
			transfer.spread();
			fail("Users of this service should really know what they're doing and another spread could indicate flaws");
		} catch (IllegalStateException e) {
			// expected
		}
	}

}
