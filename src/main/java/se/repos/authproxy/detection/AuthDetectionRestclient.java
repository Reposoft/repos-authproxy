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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.repos.authproxy.AuthFailedException;
import se.repos.authproxy.AuthDetection;
import se.repos.restclient.HttpStatusError;

public class AuthDetectionRestclient implements AuthDetection {
	
	private static final Logger logger = LoggerFactory.getLogger(AuthDetectionSvnKit.class);	
	
	private static final Pattern HEADER_PATTERN = Pattern.compile("Basic realm=\"(.*)\""); 
	
	@Override
	public void analyze(Throwable e) throws AuthFailedException {
		if (e instanceof HttpStatusError) {
			HttpStatusError h = (HttpStatusError) e;
			logger.info("REST authentication failure from service detected.", h);
			if (h.getHttpStatus() == 401) {
				List<String> header = h.getHeaders().get("WWW-Authenticate");
				if (header == null || header.size() == 0) {
					logger.error("Auth proxy received 401 status without authentication header");
				} else if (header.size() > 1) {
					logger.error("More than one WWW-Authenticate header found. Can't detect realm.");
				} else {
					Matcher m = HEADER_PATTERN.matcher(header.get(0));
					if (m.matches()) {
						throw new AuthFailedException("BASIC auth failure detected", m.group(1));
					} else {
						logger.warn("WWW-Authenticate header has unsupported contents: {}", header.get(0));
					}
				}
			}
		}
		// traverse all causes
		if (e.getCause() != null) {
			this.analyze(e.getCause());
		}		
	}

}
