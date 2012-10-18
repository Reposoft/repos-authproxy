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

import se.repos.authproxy.ReposCurrentUser;

/**
 * Facilitates transfer of authproxy credentials to a different execution contetxt,
 * as a pre-emptive means to avoid authentication exceptions where authentication can not be requested.
 * <p>
 * Could violate user iśolation.
 * Only for use with thourogh understanding of authproxy use in the current application.
 * <p>
 * Different scenarios require different storage, impls represent a storage strategy while using
 * {@link TransferMethodFactory} to handle different {@link ReposCurrentUser} impls.
 */
public interface AuthproxyTrustedTransfer {

	/**
	 * Saves, for next {@link #spread()}, credentials from this particular moment.
	 */
	void capture();
	
	/**
	 * Allowed once per {@link #capture()}.
	 */
	void spread();

	/**
	 * Removes credentials from the target context.
	 * Impls will probably not be able to restore the credentials that were before.
	 * Also not entirely sure that all impls will be able to support this.
	 */
	void undo();
	
}
