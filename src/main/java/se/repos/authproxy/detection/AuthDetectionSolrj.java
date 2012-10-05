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

import se.repos.authproxy.AuthDetection;
import se.repos.authproxy.AuthFailedException;

public class AuthDetectionSolrj implements AuthDetection {

	/*
	org.apache.solr.common.SolrException: Server at http://localhost:49992/solr returned non ok status:401, message:Unauthorized
	at org.apache.solr.client.solrj.impl.HttpSolrServer.request(HttpSolrServer.java:373)
	at org.apache.solr.client.solrj.impl.HttpSolrServer.request(HttpSolrServer.java:182)
	at org.apache.solr.client.solrj.request.QueryRequest.process(QueryRequest.java:90)
	at org.apache.solr.client.solrj.SolrServer.query(SolrServer.java:324)
	 */
	
	@Override
	public void analyze(Throwable e) throws AuthFailedException {
		System.out.println("At " + e);
		if ("org.apache.solr.common.SolrException".equals(e.getClass().getName())) {
			// svn: Authentication required for '<http://localhost:49999> See-if-svnkit-detects-this-realm
			Matcher matcher = Pattern.compile("Server at \\S+ returned non ok status:401, message:Unauthorized").matcher(e.getMessage());
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
