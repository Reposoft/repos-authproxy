package se.repos.authproxy.http;

/**
 * Used when the webapp sits behind an Apache AJP proxy
 * at a location that enforces BASIC authentication.
 * See http://httpd.apache.org/docs/2.2/mod/mod_proxy_ajp.html#env
 * Custom environment variable might be needed in order to
 * pass on the password.
 * With this solution authentication would probably have to be validated
 * before sent through AJP, so the authproxy concept wouldn't be needed.
 */
public class ReposRequireAjpEnvLoginFilter {

}
