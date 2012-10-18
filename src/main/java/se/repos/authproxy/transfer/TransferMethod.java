package se.repos.authproxy.transfer;

public interface TransferMethod {

	void spread(String username, String password);

	/**
	 * Clear spread credentials, ideally but not necessarily restoring previous state.
	 */
	void undo();
	
}
