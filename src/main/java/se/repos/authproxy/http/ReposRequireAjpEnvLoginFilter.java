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
