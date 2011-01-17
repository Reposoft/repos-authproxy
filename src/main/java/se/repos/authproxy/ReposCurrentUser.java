package se.repos.authproxy;

public abstract class ReposCurrentUser {

	/**
	 * This class is used statically.
	 */
	private ReposCurrentUser() {
	}
	
	public static String getUsername() {
		throw new UnsupportedOperationException("not implemented");
	}
	
	public static void setUsername(String username) {
		if (getUsername() != null) {
			throw new IllegalStateException("Username already set");
		}
		throw new UnsupportedOperationException("not implemented");
	}
	
	public static void clearUsername() {
		throw new UnsupportedOperationException("not implemented");
	}
	
}
