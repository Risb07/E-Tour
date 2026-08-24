package com.etour.security.oauth2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import com.etour.entity.User;

/**
 * Non-OIDC counterpart to {@link GoogleOidcUserService}.
 *
 * <p>With the current configuration this does not run, because the
 * {@code openid} scope puts the login on the OIDC path. It is registered
 * anyway so that narrowing the scopes to {@code profile email} - or adding a
 * second, non-OIDC provider later - keeps working with identical account
 * provisioning rather than falling back to Spring's default, which would not
 * create the eTour user at all.
 */
@Service
@Conditional(GoogleOAuthEnabledCondition.class)
public class GoogleOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger log = LoggerFactory.getLogger(GoogleOAuth2UserService.class);

    private final GoogleUserProvisioningService provisioningService;

    public GoogleOAuth2UserService(GoogleUserProvisioningService provisioningService) {
        this.provisioningService = provisioningService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        try {
            User user = provisioningService.provision(oAuth2User.getAttributes());
            return new EtourOAuth2User(user, oAuth2User.getAttributes());
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            log.error("Google sign-in failed while provisioning the user", ex);
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("provisioning_failed"),
                    "Could not complete Google sign-in.", ex);
        }
    }
}
