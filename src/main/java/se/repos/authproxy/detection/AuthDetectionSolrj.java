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
