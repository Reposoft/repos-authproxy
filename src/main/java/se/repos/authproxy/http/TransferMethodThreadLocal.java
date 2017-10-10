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