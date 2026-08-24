package com.etour.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Tells the frontend which sign-in methods this deployment actually has
 * configured, so the login page can show a "Continue with Google" button only
 * when clicking it would work.
 *
 * <p>Lives at {@code /api/auth/providers}, which the existing security rules
 * already make public via the {@code /api/auth/**} permitAll entry - no change
 * to {@code SecurityConfig} was needed.
 *
 * <p>Only booleans are returned. The client id is never exposed, because with
 * the backend-driven authorization code flow the browser has no use for it.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthProvidersController {

    private final boolean googleEnabled;

    public AuthProvidersController(
            @Value("${app.oauth2.google.client-id:}") String googleClientId,
            @Value("${app.oauth2.google.client-secret:}") String googleClientSecret) {

        this.googleEnabled = !googleClientId.isBlank() && !googleClientSecret.isBlank();
    }

    @GetMapping("/providers")
    public Map<String, Object> providers() {
        return Map.of(
                "google", googleEnabled,
                // The URL the browser must be sent to in order to start the
                // flow. Kept server-side so the path stays in one place.
                "googleAuthorizationUrl", "/oauth2/authorization/google");
    }
}
