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
