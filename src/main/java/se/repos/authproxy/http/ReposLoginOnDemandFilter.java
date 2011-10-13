package se.repos.authproxy.http;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import se.repos.authproxy.AuthRequiredException;

/**
 * Catches {@link AuthRequiredException} and requests authentication
 * from client, leading to a new request with ReposAuthHolder populated.
 * <p>
 * If {@link AuthRequiredException} can not produce a realm,
 * realm must be configured in this filter.
 * <p>
 * Web browsers usually remember the paths where credentials should be sent
 * with the requests so the try-retry roundtrip is normally done only once
 * per service.
 */
public class ReposLoginOnDemandFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Method not implemented");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Method not implemented");
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Method not implemented");
	}

}
