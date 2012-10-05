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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import se.repos.authproxy.AuthFailedException;
import se.repos.authproxy.AuthDetection;

/**
 * Detects the typical SvnKit svn authentication error, without runtime dependency to SvnKit.
 */
public class AuthDetectionSvnKit implements AuthDetection {

	@Override
	public void analyze(Throwable e) throws AuthFailedException {
		if ("org.tmatesoft.svn.core.SVNAuthenticationException".equals(e.getClass().getName())) {
			// svn: Authentication required for '<http://localhost:49999> See-if-svnkit-detects-this-realm
			Matcher matcher = Pattern.compile(".*Authentication required for '\\S+ (.*)'").matcher(e.getMessage());
			if (matcher.matches()) {
				throw new AuthFailedException(e, matcher.group(1));
			}
		}
		// traverse all causes
		if (e.getCause() != null) {
			this.analyze(e.getCause());
		}
	}

}
