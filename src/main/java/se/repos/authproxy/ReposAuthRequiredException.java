package se.repos.authproxy;

public class ReposAuthRequiredException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * 
	 * @param realm realm name for login, usually from WWW-Authenticate header value
	 */
	public ReposAuthRequiredException(String realm) {
		throw new UnsupportedOperationException("not implemented");
	}
	
}
