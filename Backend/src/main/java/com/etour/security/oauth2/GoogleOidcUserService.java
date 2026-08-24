package com.etour.security.oauth2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import com.etour.entity.User;

/**
 * The user service that actually runs for Google sign-in.
 *
 * <p>The Google registration requests the {@code openid} scope, so Spring
 * Security treats the login as OpenID Connect: it validates the ID token
 * signature against Google's JWK set, then calls this class rather than the
 * plain OAuth2 one. Registering only a {@code DefaultOAuth2UserService} would
 * silently have no effect.
 *
 * <p>All this does is delegate to the standard {@link OidcUserService} to
 * fetch the claims, hand them to {@link GoogleUserProvisioningService}, and
 * wrap the result so the success handler can read the eTour account back out.
 */
@Service
@Conditional(GoogleOAuthEnabledCondition.class)
public class GoogleOidcUserService extends OidcUserService {

    private static final Logger log = LoggerFactory.getLogger(GoogleOidcUserService.class);

    private final GoogleUserProvisioningService provisioningService;

    public GoogleOidcUserService(GoogleUserProvisioningService provisioningService) {
        this.provisioningService = provisioningService;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {

        OidcUser oidcUser = super.loadUser(userRequest);

        try {
            User user = provisioningService.provision(oidcUser.getAttributes());
            return new EtourOidcUser(user, oidcUser);
        } catch (AuthenticationException ex) {
            // Already a message we chose - let the failure handler show it.
            throw ex;
        } catch (RuntimeException ex) {
            // Anything else must still surface as an AuthenticationException,
            // otherwise Spring Security turns it into a raw 500 and the user is
            // left on a blank page instead of being redirected back to login.
            log.error("Google sign-in failed while provisioning the user", ex);
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("provisioning_failed"),
                    "Could not complete Google sign-in.", ex);
        }
    }
}
