package com.etour.security.oauth2;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * A second, higher-priority security filter chain that handles nothing but
 * the Google OAuth 2.0 round trip.
 *
 * <p>This is deliberately a separate chain rather than an addition to
 * {@code SecurityConfig}, for three reasons:
 *
 * <ol>
 *   <li><b>The existing chain is not modified at all.</b> Every authorization
 *       rule, the JWT filter and the CORS setup stay exactly as they were.</li>
 *   <li><b>Statelessness is preserved where it matters.</b> The authorization
 *       code flow must keep the {@code state} parameter and PKCE verifier
 *       somewhere between redirecting to Google and receiving the callback,
 *       which means an HTTP session. Confining that to these two URLs lets the
 *       whole {@code /api/**} surface remain {@code STATELESS} as before.</li>
 *   <li><b>The API's error behaviour is unchanged.</b> Calling
 *       {@code oauth2Login()} on the main chain would replace its
 *       authentication entry point, so unauthenticated {@code /api/**} calls
 *       would start returning a 302 to Google instead of the status code the
 *       React client already handles.</li>
 * </ol>
 *
 * <p>{@code @Order(1)} puts this ahead of the unordered chain in
 * {@code SecurityConfig}, which keeps its implicit lowest precedence and so
 * still catches every other request.
 *
 * <p>The two URLs are Spring Security's own defaults:
 * <ul>
 *   <li>{@code GET /oauth2/authorization/google} - starts the flow. This is
 *       what the "Continue with Google" button points at.</li>
 *   <li>{@code GET /login/oauth2/code/google} - the redirect URI Google calls
 *       back on. This must be registered in the Google Cloud Console.</li>
 * </ul>
 */
@Configuration
@Conditional(GoogleOAuthEnabledCondition.class)
public class OAuth2SecurityConfig {

    private final GoogleOidcUserService googleOidcUserService;
    private final GoogleOAuth2UserService googleOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler successHandler;
    private final OAuth2AuthenticationFailureHandler failureHandler;

    public OAuth2SecurityConfig(GoogleOidcUserService googleOidcUserService,
                                GoogleOAuth2UserService googleOAuth2UserService,
                                OAuth2AuthenticationSuccessHandler successHandler,
                                OAuth2AuthenticationFailureHandler failureHandler) {
        this.googleOidcUserService = googleOidcUserService;
        this.googleOAuth2UserService = googleOAuth2UserService;
        this.successHandler = successHandler;
        this.failureHandler = failureHandler;
    }

    @Bean
    @Order(1)
    SecurityFilterChain oauth2SecurityFilterChain(HttpSecurity http) throws Exception {

        http
                .securityMatcher("/oauth2/**", "/login/oauth2/**")
                // These are top-level browser navigations, not XHR, so there is
                // no CSRF token to carry. The flow's own `state` parameter is
                // what protects the callback against forgery.
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                // The `openid` scope means Google logins take
                                // the OIDC path, so this is the one that runs.
                                .oidcUserService(googleOidcUserService)
                                // Registered for the non-OIDC case; see
                                // GoogleOAuth2UserService for why.
                                .userService(googleOAuth2UserService))
                        .successHandler(successHandler)
                        .failureHandler(failureHandler));

        return http.build();
    }
}
