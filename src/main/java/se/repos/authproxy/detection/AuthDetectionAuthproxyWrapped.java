package se.repos.authproxy.detection;

import se.repos.authproxy.AuthDetection;
import se.repos.authproxy.AuthFailedException;

public class AuthDetectionAuthproxyWrapped implements AuthDetection {

	@Override
	public void analyze(Throwable e) throws AuthFailedException {
		if (e == null) return;
		if (e instanceof AuthFailedException) throw (AuthFailedException) e;
		analyze(e.getCause());
	}

}
