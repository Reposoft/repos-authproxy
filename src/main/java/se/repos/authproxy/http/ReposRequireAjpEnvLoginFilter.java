package se.repos.authproxy.http;

/**
 * Used when the webapp sits behind an Apache AJP proxy
 * at a location that enforces BASIC authentication.
 * See http://httpd.apache.org/docs/2.2/mod/mod_proxy_ajp.html#env
 * Custom environment variable might be needed in order to
 * pass on the 
 */
public class ReposRequireAjpEnvLoginFilter {

}
