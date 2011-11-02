package se.repos.authproxy.detection;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.repos.authproxy.AuthFailedException;
import se.repos.authproxy.AuthDetection;
import se.repos.restclient.HttpStatusError;

public class AuthDetectionRestclient implements AuthDetection {

	// note that someone must still load the class for this to happen
	static {
		known.add(new AuthDetectionRestclient());
	}	
	
	private static final Logger logger = LoggerFactory.getLogger(AuthDetectionSvnKit.class);	
	
	@Override
	public void analyze(Throwable e) throws AuthFailedException {
		if (e instanceof HttpStatusError) {
			HttpStatusError h = (HttpStatusError) e;
			logger.info("REST authentication failure from service detected.", h);
			if (h.getHttpStatus() == 401) {
				List<String> header = h.getHeaders().get("WWW-Authenticate");
				if (header != null && header.size() > 0) {
					throw new UnsupportedOperationException("TODO detect realm");
				} else {
					// Warn because this won't work with on-demand authentication
					logger.error("Auth proxy received 401 status without authentication header");
				}
			}
		}
		// traverse all causes
		if (e.getCause() != null) {
			this.analyze(e.getCause());
		}		
	}

}
