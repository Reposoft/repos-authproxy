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
package se.repos.authproxy.detection;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;

import se.repos.authproxy.AuthDetection;
import se.repos.authproxy.AuthFailedException;
import se.repos.restclient.HttpStatusError;
import se.repos.restclient.ResponseHeaders;

/**
 * Verify that authentication errors in Repos Restclient
 * can be caught and easily forwarded to auth filter for retry.
 */
public class ReposRestclientTest {

	@Test
	public void test() {
		ResponseHeaders h = mock(ResponseHeaders.class);
		when(h.get("WWW-Authenticate")).thenReturn(Arrays.asList("Basic realm=\"My Realm\""));
		when(h.getStatus()).thenReturn(401);
		
		AuthDetection a = AuthDetection.all; // restclient support should be out of the box
		
		try {
			a.analyze(new HttpStatusError("http://x/y", h, "<html/>"));
			fail("Should have detected restclient authentication error in exception cause");
		} catch (AuthFailedException f) {
			assertEquals("My Realm", f.getRealm());
		}
	}
	
	@Test
	public void testWrapped() {
		AuthDetection a = AuthDetection.all; // restclient support should be out of the box

		ResponseHeaders h = mock(ResponseHeaders.class);
		when(h.get("WWW-Authenticate")).thenReturn(Arrays.asList("Basic realm=\"My Realm 2\""));
		when(h.getStatus()).thenReturn(401);
		HttpStatusError e = new HttpStatusError("http://x/y", h, "<html/>");
		
		try {
			// wrap th exception two times
			a.analyze(new RuntimeException("Some error occurred", new IOException(e)));
			fail("Should have detected restclient authentication error in exception cause");
		} catch (AuthFailedException f) {
			assertEquals("My Realm 2", f.getRealm());
		}
	}

	@Test
	public void testNot401() {
		ResponseHeaders h = mock(ResponseHeaders.class);
		when(h.get("WWW-Authenticate")).thenReturn(Arrays.asList("Basic realm=\"My Realm\""));
		when(h.getStatus()).thenReturn(500);
		
		AuthDetection a = AuthDetection.all; // restclient support should be out of the box
		
		try {
			a.analyze(new HttpStatusError("http://x/y", h, "<html/>"));			
		} catch (AuthFailedException f) {
			fail("Only http status 401 is authentication");
		}
	}

	@Test
	public void test401ButNoHeader() {
		ResponseHeaders h = mock(ResponseHeaders.class);
		when(h.get("WWW-Authenticate")).thenReturn(null);
		when(h.getStatus()).thenReturn(401);
		
		AuthDetection a = AuthDetection.all; // restclient support should be out of the box
		
		try {
			a.analyze(new HttpStatusError("http://x/y", h, "<html/>"));			
		} catch (AuthFailedException f) {
			fail("401 without WWW-Authenticate header might be a server HTTP error");
		}
	}	
	
	@Test
	public void testNotBasic() {
		ResponseHeaders h = mock(ResponseHeaders.class);
		when(h.get("WWW-Authenticate")).thenReturn(Arrays.asList("Something not Basic"));
		when(h.getStatus()).thenReturn(500);
		
		AuthDetection a = AuthDetection.all; // restclient support should be out of the box
		
		try {
			a.analyze(new HttpStatusError("http://x/y", h, "<html/>"));			
		} catch (AuthFailedException f) {
			fail("We can only support BASIC auth so don't interrupt if WWW-Authenticate header is not known");
		}
	}
	
}
