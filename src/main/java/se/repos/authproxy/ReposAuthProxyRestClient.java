package se.repos.authproxy;

import java.io.IOException;

import se.repos.restclient.HttpStatusError;
import se.repos.restclient.ResponseHeaders;
import se.repos.restclient.RestClient;
import se.repos.restclient.RestResponse;

/**
 * Recognizes status errors from any {@link RestClient} implementation,
 * and transforms authentication errors to the runtime exception
 * that causes retry.
 * <p>
 * You can instead throw {@link AuthRequiredException} using custom code.
 * A valid argument being that it is questionable whether code should be written
 * without awareness of the risk for retry.
 */
public class ReposAuthProxyRestClient implements RestClient {

	private RestClient client;

	public ReposAuthProxyRestClient(RestClient realClient) {
		this.client = realClient;
	}

	@Override
	public void get(String uri, RestResponse response) throws IOException,
			HttpStatusError {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Method not implemented");
	}

	@Override
	public ResponseHeaders head(String uri) throws IOException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Method not implemented");
	}

}
