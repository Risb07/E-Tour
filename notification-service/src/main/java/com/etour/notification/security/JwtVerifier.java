package com.etour.notification.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * Verifies an eTour JWT.
 *
 * <p>The key derivation below is copied deliberately from the Java backend's
 * {@code JwtService} and matches the .NET backend's {@code JwtSigningKeyProvider}:
 * prefer a base64 secret that decodes to at least 32 bytes, otherwise SHA-256
 * the raw string. Getting this wrong would not fail loudly - it would just
 * reject every token - so it has to mirror both of them exactly.
 *
 * <p>This establishes <b>who</b> the caller is. It cannot establish whether they
 * are an admin: eTour tokens carry only {@code sub} and {@code iat}/{@code exp},
 * with the role re-read from the database on every request by both backends.
 * {@link AdminAuthorizer} covers that half.
 */
@Component
public class JwtVerifier {

    private static final Logger log = LoggerFactory.getLogger(JwtVerifier.class);

    private final String secret;

    public JwtVerifier(@Value("${security.jwt.secret:}") String secret) {
        this.secret = secret;
        if (secret == null || secret.isBlank()) {
            log.warn("JWT_SECRET is not set - every authenticated endpoint will reject callers. "
                    + "Set the same secret the eTour backends use.");
        }
    }

    /** @return the token's subject (the user's email), or empty if it is missing, expired or forged. */
    public Optional<String> verifyAndGetSubject(String token) {
        if (token == null || token.isBlank() || secret == null || secret.isBlank()) {
            return Optional.empty();
        }

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(signingKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String subject = claims.getSubject();
            return subject == null || subject.isBlank() ? Optional.empty() : Optional.of(subject);

        } catch (Exception ex) {
            // Expired, malformed and bad-signature all mean the same thing to a
            // caller - "not authenticated" - and distinguishing them in the
            // response would only help someone probing tokens.
            log.debug("Rejected a token: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    private Key signingKey() {
        byte[] keyBytes = null;
        try {
            byte[] decoded = Decoders.BASE64.decode(secret);
            if (decoded.length >= 32) {
                keyBytes = decoded;
            }
        } catch (IllegalArgumentException ex) {
            // Not base64 - fall through and hash it as a plain string.
        }

        if (keyBytes == null) {
            keyBytes = sha256(secret);
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }

    private static byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is required to derive the JWT signing key", ex);
        }
    }
}
