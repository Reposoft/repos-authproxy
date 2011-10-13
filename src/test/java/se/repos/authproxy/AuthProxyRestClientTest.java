package se.repos.authproxy;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import se.repos.restclient.HttpStatusError;
import se.repos.restclient.RestClient;
import se.repos.restclient.RestResponse;

public class AuthProxyRestClientTest {

	@Test
	@Ignore // not implemented
	public void testGet() {
		RestClient realClient = mock(RestClient.class);
		RestClient client = new ReposAuthProxyRestClient(realClient);
		RestResponse mockResponse = mock(RestResponse.class);
		//when(client.get("x", mockResponse))
		try {
			client.get("x", mockResponse);
		} catch (HttpStatusError e) {
			// TODO Auto-generated catch block
			throw new RuntimeException("Error not handled", e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException("Error not handled", e);
		}
	}

	@Test
	@Ignore // not implemented
	public void testHead() {
		fail("Not yet implemented");
	}

}
