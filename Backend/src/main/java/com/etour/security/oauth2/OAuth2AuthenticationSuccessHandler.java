package com.etour.security.oauth2;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.etour.entity.User;
import com.etour.security.JwtService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * The bridge between Google's OAuth 2.0 flow and eTour's own stateless JWT
 * session.
 *
 * <p>By the time this runs, Google has authenticated the user and
 * {@link GoogleOAuth2UserService} has resolved (or created) the matching eTour
 * account. All that is left is to issue the very same JWT that
 * {@code /api/auth/login} issues - via the shared {@code JwtService} - and hand
 * the browser back to the SPA.
 *
 * <p>The token travels as a query parameter on a redirect to the frontend
 * callback route. The SPA reads it, stores it exactly like a password login's
 * token, and immediately replaces the URL so the token does not linger in
 * browser history.
 */
@Component
@Conditional(GoogleOAuthEnabledCondition.class)
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler.class);

    private final JwtService jwtService;

    @Value("${app.oauth2.frontend-redirect-uri}")
    private String frontendRedirectUri;

    public OAuth2AuthenticationSuccessHandler(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        if (response.isCommitted()) {
            log.warn("Response already committed - cannot redirect to {}", frontendRedirectUri);
            return;
        }

        String targetUrl = buildTargetUrl(authentication);

        // The rest of the application is stateless; leaving an authenticated
        // SecurityContext behind after the redirect would serve no purpose.
        SecurityContextHolder.clearContext();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private String buildTargetUrl(Authentication authentication) {

        if (!(authentication.getPrincipal() instanceof EtourOAuthPrincipal principal)) {
            // Should be unreachable - both custom user services produce an
            // EtourOAuthPrincipal - but failing loudly beats leaving the
            // browser half logged in.
            return errorUrl("Unexpected sign-in state. Please try again.");
        }

        User user = principal.getUser();
        String token = jwtService.generateToken(user.getEmail());

        return UriComponentsBuilder.fromUriString(frontendRedirectUri)
                .queryParam("token", encode(token))
                .queryParam("userId", user.getUserId())
                .queryParam("firstName", encode(nullToEmpty(user.getFirstName())))
                .queryParam("lastName", encode(nullToEmpty(user.getLastName())))
                .queryParam("email", encode(nullToEmpty(user.getEmail())))
                .queryParam("role", encode(user.getRole().getRoleName()))
                .build(true)
                .toUriString();
    }

    private String errorUrl(String message) {
        return UriComponentsBuilder.fromUriString(frontendRedirectUri)
                .queryParam("error", encode(message))
                .build(true)
                .toUriString();
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
