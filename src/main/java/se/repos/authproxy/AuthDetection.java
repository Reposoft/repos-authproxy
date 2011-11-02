package se.repos.authproxy;

import java.util.HashSet;
import java.util.Set;

import se.repos.authproxy.http.ReposLoginOnDemandFilter;

/**
 * Bridges exception handling to authproxy.
 * <p>
 * Used by filters such as {@link ReposLoginOnDemandFilter} to
 * analyze exceptions from the filter chain and see if the user
 * should be prompted for authentication.
 * <p>
 * Implementations are encouraged to add themselves to {@link #known}
 * and, to be compatible with {@link ReposLoginOnDemandFilter},
 * figure out the realm and set on the {@link AuthFailedException}.
 * <p>
 * If realm detection can not be done simply by looking at exceptions,
 * libraries must explicitly throw {@link AuthFailedException} or
 * {@link AuthRequiredException} from code for example by using
 * {@link ReposCurrentUser#getUsernameRequired(String)}.
 * Another valid reason for explicitly implementing authproxy exceptions
 * is that it makes code aware of the retries that HTTP authentication uses.
 */
public interface AuthDetection {

	/**
	 * Can be used to statically add new analyzers that should
	 * be invoked when running {@link #all}.
	 * 
	 * Should be considered add-only, don't remove or replace items (could be enforced by list impl).
	 */
	public static final Set<AuthDetection> known = new HashSet<AuthDetection>();
	
	/**
	 * Default analyzer for static use, invoking all currently {@link #known}.
	 */
	public static final AuthDetection all = new AuthDetection() {
		@Override
		public void analyze(Throwable e) throws AuthFailedException {
			if (known.size() == 0) System.err.println("Static AuthDetection list is empty. Running all is of no use.");
			for (AuthDetection a : known) {
				a.analyze(e);
			}
		}
	};

	/**
	 * Detects authentication errors in exception.
	 * Implementations may traverse the {@link Throwable#getCause()}s if desired.
	 * @param e The exception
	 * @throws AuthFailedException If the exception is an authentication error,
	 *  {@link ReposLoginOnDemandFilter} requires realm to be set
	 */
	void analyze(Throwable e) throws AuthFailedException;
	
}
