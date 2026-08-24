package com.etour.security.oauth2;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

/**
 * Builds the Google client registration from eTour's own configuration
 * properties.
 *
 * <p>{@link CommonOAuth2Provider#GOOGLE} supplies the standard endpoints
 * (authorization, token, JWK set and userinfo), the {@code openid profile
 * email} scopes and {@code sub} as the name attribute, so only the credentials
 * and redirect URI need filling in. Using it keeps this class small and means
 * the endpoint URLs are maintained by Spring Security rather than hardcoded
 * here.
 *
 * <p>The whole class is skipped unless a client id and secret are configured -
 * see {@link GoogleOAuthEnabledCondition}.
 */
@Configuration
@Conditional(GoogleOAuthEnabledCondition.class)
public class GoogleOAuth2ClientConfig {

    /** Registration id. It is what makes the URLs .../google, so don't rename it. */
    public static final String REGISTRATION_ID = "google";

    @Value("${app.oauth2.google.client-id}")
    private String clientId;

    @Value("${app.oauth2.google.client-secret}")
    private String clientSecret;

    @Value("${app.oauth2.google.redirect-uri}")
    private String redirectUri;

    @Bean
    ClientRegistrationRepository clientRegistrationRepository() {

        ClientRegistration google = CommonOAuth2Provider.GOOGLE
                .getBuilder(REGISTRATION_ID)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .redirectUri(redirectUri)
                .build();

        return new InMemoryClientRegistrationRepository(google);
    }
}
