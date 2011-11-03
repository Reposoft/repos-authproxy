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
