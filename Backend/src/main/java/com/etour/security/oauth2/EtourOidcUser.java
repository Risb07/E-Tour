package com.etour.security.oauth2;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import com.etour.entity.User;

/**
 * The authenticated principal for the duration of the Google login round trip.
 *
 * <p>Wraps the {@link OidcUser} Spring Security built (delegating the ID token
 * and claims to it) and carries the resolved eTour {@link User} alongside, so
 * {@link OAuth2AuthenticationSuccessHandler} can mint the JWT without a second
 * database lookup.
 *
 * <p>This principal only lives inside the OAuth2 filter chain. Every
 * subsequent request is authenticated purely from the JWT by
 * {@code JwtAuthenticationFilter}, exactly as a password login is. Authorities
 * use the same {@code ROLE_*} format as {@code CustomUserDetails}, so nothing
 * downstream can tell the two sign-in routes apart.
 */
public class EtourOidcUser implements OidcUser, EtourOAuthPrincipal {

    private final User user;
    private final OidcUser delegate;

    public EtourOidcUser(User user, OidcUser delegate) {
        this.user = user;
        this.delegate = delegate;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public Map<String, Object> getClaims() {
        return delegate.getClaims();
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return delegate.getUserInfo();
    }

    @Override
    public OidcIdToken getIdToken() {
        return delegate.getIdToken();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return delegate.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().getRoleName()));
    }

    @Override
    public String getName() {
        return user.getEmail();
    }
}
