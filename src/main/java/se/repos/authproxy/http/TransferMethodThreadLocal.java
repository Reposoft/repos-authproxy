package se.repos.authproxy.http;

import se.repos.authproxy.transfer.TransferMethod;

public class TransferMethodThreadLocal implements TransferMethod {

	private ReposCurrentUserThreadLocal threadLocal;

	public TransferMethodThreadLocal(ReposCurrentUserThreadLocal authHolder) {
		this.threadLocal = authHolder;
	}

	@Override
	public void spread(String username, String password) {
		threadLocal.provide(username, password);
	}

	@Override
	public void undo() {
		threadLocal.clear();
	}
	
}
