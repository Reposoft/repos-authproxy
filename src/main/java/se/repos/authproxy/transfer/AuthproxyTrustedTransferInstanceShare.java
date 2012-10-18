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
package se.repos.authproxy.transfer;

import javax.inject.Inject;

import se.repos.authproxy.ReposCurrentUser;

/**
 * Transfers credentials through a single instance of this class,
 * for example to a spawned thread.
 * 
 * Note that after transfer the instance is no longer required in the new context,
 * meaning that credentials can be leaked at improper use.
 */
public class AuthproxyTrustedTransferInstanceShare implements
		AuthproxyTrustedTransfer {

	private ReposCurrentUser currentUser;
	private TransferMethod spreadMethod;

	private boolean captured = false;
	private String u = null;
	private String p = null;
	
	/**
	 * @param currentUser
	 * @throws IllegalArgumentException if the type of {@link ReposCurrentUser} is not supported
	 */
	@Inject
	public AuthproxyTrustedTransferInstanceShare(ReposCurrentUser currentUser)
			throws IllegalArgumentException {
		this.currentUser = currentUser;
		this.spreadMethod = TransferMethodFactory.get(currentUser);
	}
	
	@Override
	public void capture() {
		if (captured) {
			throw new IllegalStateException("Can not continue because authentication has already been captured but not transferred");
		}
		this.u = currentUser.getUsername();
		this.p = currentUser.getPassword();
		captured = true;
	}

	@Override
	public void spread() {
		if (!captured) {
			throw new IllegalStateException("Can not continue because authentication has not been captured for transfer or has already been transferred");
		}
		this.spreadMethod.spread(this.u, this.p);
		captured = false; // only one transfer per capture
	}

	@Override
	public void undo() {
		this.spreadMethod.undo();
	}
	
}
