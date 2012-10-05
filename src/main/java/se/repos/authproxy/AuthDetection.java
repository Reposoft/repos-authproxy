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
package se.repos.authproxy;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.LoggerFactory;

import se.repos.authproxy.detection.AuthDetectionAuthproxyWrapped;
import se.repos.authproxy.detection.AuthDetectionRestclient;
import se.repos.authproxy.detection.AuthDetectionSvnKit;
import se.repos.authproxy.http.ReposLoginOnDemandFilter;

/**
 * Bridges exception handling to authproxy.
 * <p>
 * Used by filters such as {@link ReposLoginOnDemandFilter} to
 * analyze exceptions from the filter chain and see if the user
 * should be prompted for authentication.
 * <p>
 * Implementations are encouraged to add themselves to {@link #known}
 * and, to be compatible with {@link ReposLoginOnDemandFilter},
 * figure out the realm and set on the {@link AuthFailedException}.
 * <p>
 * If realm detection can not be done simply by looking at exceptions,
 * libraries must explicitly throw {@link AuthFailedException} or
 * {@link AuthRequiredException} from code for example by using
 * {@link ReposCurrentUser#getUsernameRequired(String)}.
 * Another valid reason for explicitly implementing authproxy exceptions
 * is that it makes code aware of the retries that HTTP authentication uses.
 */
public interface AuthDetection {
	
	/**
	 * Can be used to statically add new analyzers that should
	 * be invoked when running {@link #all}.
	 * 
	 * Should be considered add-only, don't remove or replace items (could be enforced by set impl).
	 * 
	 * Initialized with all impls in the detection package. No need to be flexible
	 * here, as we plan an init param in filters to set an arbitrary implementation.
	 */
	public static final Set<AuthDetection> known = new HashSet<AuthDetection>(
			Arrays.asList(
				new AuthDetectionAuthproxyWrapped(),
				new AuthDetectionRestclient(),
				new AuthDetectionSvnKit()
			));
	
	/**
	 * Default analyzer for static use, invoking all currently {@link #known}.
	 */
	public static final AuthDetection all = new AuthDetection() {
		@Override
		public void analyze(Throwable e) throws AuthFailedException {
			for (AuthDetection a : known) {
				try {
					a.analyze(e);
				} catch (AuthFailedException f) {
					LoggerFactory.getLogger(AuthDetection.class).info("Authentication error detected by {} in {}", a, e);
					throw f;
				}
			}
		}
	};

	/**
	 * Detects authentication errors in exception.
	 * Implementations may traverse the {@link Throwable#getCause()}s if desired.
	 * @param e The exception
	 * @throws AuthFailedException If the exception is an authentication error,
	 *  {@link ReposLoginOnDemandFilter} requires realm to be set
	 */
	void analyze(Throwable e) throws AuthFailedException;
	
}
