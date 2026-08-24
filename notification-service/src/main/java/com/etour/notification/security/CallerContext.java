package com.etour.notification.security;

import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Resolves the caller for the current request, and enforces the two access
 * levels this service has.
 *
 * <p>Kept out of the controllers so the rule "admin endpoints need a real
 * eTour admin" is written once rather than repeated per handler, where one
 * missing call would silently make an endpoint public.
 */
@Component
@RequestScope
public class CallerContext {

    private static final String BEARER = "Bearer ";

    private final HttpServletRequest request;
    private final JwtVerifier jwtVerifier;
    private final AdminAuthorizer adminAuthorizer;

    public CallerContext(HttpServletRequest request, JwtVerifier jwtVerifier, AdminAuthorizer adminAuthorizer) {
        this.request = request;
        this.jwtVerifier = jwtVerifier;
        this.adminAuthorizer = adminAuthorizer;
    }

    public Optional<String> token() {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(BEARER)) {
            return Optional.empty();
        }
        String token = header.substring(BEARER.length()).trim();
        return token.isEmpty() ? Optional.empty() : Optional.of(token);
    }

    /** @return the caller's email. @throws UnauthorizedException if the token is missing or invalid. */
    public String requireAuthenticated() {
        return token()
                .flatMap(jwtVerifier::verifyAndGetSubject)
                .orElseThrow(() -> new UnauthorizedException("A valid eTour token is required"));
    }

    /**
     * @throws UnauthorizedException if unauthenticated
     * @throws ForbiddenException    if authenticated but not an ADMIN
     */
    public String requireAdmin() {
        String email = requireAuthenticated();
        String token = token().orElseThrow(() -> new UnauthorizedException("A valid eTour token is required"));

        if (!adminAuthorizer.isAdmin(token)) {
            throw new ForbiddenException("This operation requires an eTour administrator account");
        }
        return email;
    }
}
