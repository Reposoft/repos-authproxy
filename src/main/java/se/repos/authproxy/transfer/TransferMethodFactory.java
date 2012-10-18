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

import java.util.HashMap;
import java.util.Map;

import se.repos.authproxy.ReposCurrentUser;
import se.repos.authproxy.http.ReposCurrentUserThreadLocal;
import se.repos.authproxy.http.TransferMethodThreadLocal;

class TransferMethodFactory {

	private static Map<Class<? extends ReposCurrentUser>, Class<? extends TransferMethod>> known
		= new HashMap<Class<? extends ReposCurrentUser>, Class<? extends TransferMethod>>();
	
	static {
		known.put(ReposCurrentUserThreadLocal.class, TransferMethodThreadLocal.class);
	}
	
	static TransferMethod get(ReposCurrentUser authHolder) {
		Class<? extends TransferMethod> c = known.get(authHolder.getClass());
		if (c == null) {
			throw new IllegalArgumentException("No auth transfer method found for authproxy holder type " + authHolder.getClass());
		}
		try {
			return (TransferMethod) c.getConstructors()[0].newInstance(authHolder);
		} catch (Exception e) {
			throw new RuntimeException("Failure in internal authproxy transfer configuration", e);
		}
	}
	
	public static void add(Class<? extends ReposCurrentUser> authHoderType, Class<? extends TransferMethod> spreadType) {
		known.put(authHoderType, spreadType);
	}
	
}
