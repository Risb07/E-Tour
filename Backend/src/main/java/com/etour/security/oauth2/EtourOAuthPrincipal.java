package com.etour.security.oauth2;

import com.etour.entity.User;

/**
 * Common contract for the two principal types the Google flow can produce.
 *
 * <p>Because the Google registration requests the {@code openid} scope, Spring
 * Security treats the login as OpenID Connect and builds an {@code OidcUser}.
 * If the scope is ever narrowed to just {@code profile email} it falls back to
 * a plain {@code OAuth2User}. Both cases are supported, and this interface is
 * what lets {@link OAuth2AuthenticationSuccessHandler} mint the JWT without
 * caring which one it got.
 */
public interface EtourOAuthPrincipal {

    /** The resolved (linked or newly created) eTour account. */
    User getUser();
}
