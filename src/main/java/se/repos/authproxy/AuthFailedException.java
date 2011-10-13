package se.repos.authproxy;

/**
 * Thrown by any backend to signal that the credentials provided
 * by the auth proxy was rejected at authentication.
 * <p>
 * The auth proxy also understands {@link se.repos.restclient.HttpStatusError} with status 401.
 * <p>
 * Note that this can only be throws if credentials are avaiailable.
 * Unlike {@link AuthRequiredException} it does not provide the auth proxy
 * with a realm name, so a prompt for authentication can not be made,
 * only a retry when there is already a realm from {@link AuthRequiredException}.
 * 
 * If instead credentials are provided the exception means they were not valid.
 * 
 * However in HTTP auth there is no difference; the server will keep
 * sending 401 Authentication Required for every {@link AuthFailedException}.
 */
public class AuthFailedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public AuthFailedException(String message) {
		super(message);
	}

	public AuthFailedException(Throwable cause) {
		super(cause);
	}

}
