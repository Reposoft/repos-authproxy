package se.repos.authproxy.http;

/**
 * Inspired by Spring Security.
 * See http://static.springsource.org/spring-security/site/docs/3.1.x/apidocs/org/springframework/security/core/context/SecurityContextHolder.html
 * 
 * Compare
 * http://static.springsource.org/spring-security/site/xref/org/springframework/security/context/ThreadLocalSecurityContextHolderStrategy.html
 * with
 * http://static.springsource.org/spring-security/site/xref/org/springframework/security/context/GlobalSecurityContextHolderStrategy.html
 */
public class ReposCurrentUserThreadLocal extends ReposCurrentUserBase {

	private static ThreadLocal<String> u = new ThreadLocal<String>();
	private static ThreadLocal<String> p = new ThreadLocal<String>();
	
	@Override
	public String getUsername() {
		return u.get();
	}
	
	@Override
	public String getPassword() {
		return p.get();
	}

	@Override
	void provide(String username, String password) {
		u.set(username);
		p.set(password);
	}

	@Override
	void clear() {
		u.set(null);
		p.set(null);
	}

}
