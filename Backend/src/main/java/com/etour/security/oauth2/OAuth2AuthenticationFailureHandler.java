package com.etour.security.oauth2;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Sends the browser back to the SPA with a readable message when Google
 * sign-in does not complete - the user cancelled at the consent screen, the
 * account is disabled, the email was unverified, and so on.
 *
 * <p>Without this, Spring Security's default is to render its own error page
 * on the backend origin, stranding the user outside the React app.
 */
@Component
@Conditional(GoogleOAuthEnabledCondition.class)
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private static final Logger log = LoggerFactory.getLogger(OAuth2AuthenticationFailureHandler.class);

    @Value("${app.oauth2.frontend-redirect-uri}")
    private String frontendRedirectUri;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        log.warn("Google sign-in failed: {}", exception.getMessage());

        String targetUrl = UriComponentsBuilder.fromUriString(frontendRedirectUri)
                .queryParam("error", URLEncoder.encode(userFacingMessage(exception), StandardCharsets.UTF_8))
                .build(true)
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    /**
     * Only messages we wrote ourselves are shown to the user. Anything raised
     * by the OAuth2 machinery itself could leak configuration details (client
     * ids, token endpoint responses), so those collapse to a generic line.
     */
    private String userFacingMessage(AuthenticationException exception) {

        if (exception instanceof OAuth2AuthenticationException oauthEx) {
            String code = oauthEx.getError() == null ? "" : String.valueOf(oauthEx.getError().getErrorCode());
            switch (code) {
                case "email_missing":
                case "email_unverified":
                case "account_disabled":
                    return exception.getMessage();
                case "access_denied":
                    return "Google sign-in was cancelled.";
                default:
                    break;
            }
        }

        return "Google sign-in failed. Please try again or use your email and password.";
    }
}
