package com.etour.security.oauth2;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.etour.entity.User;

/**
 * The authenticated principal for the duration of the Google login round
 * trip. It carries the resolved eTour {@link User} alongside Google's raw
 * attributes so the success handler can mint a JWT without a second database
 * lookup.
 *
 * <p>This principal only ever lives inside the OAuth2 filter chain - the rest
 * of the application is stateless and continues to authenticate purely from
 * the JWT via {@code JwtAuthenticationFilter} and {@code CustomUserDetails}.
 * Authorities are mapped to the same {@code ROLE_*} format those use, so
 * nothing downstream can tell the two paths apart.
 */
public class EtourOAuth2User implements OAuth2User, EtourOAuthPrincipal {

    private final User user;
    private final Map<String, Object> attributes;

    public EtourOAuth2User(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
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
