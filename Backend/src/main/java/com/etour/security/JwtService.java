package com.etour.security;

import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    // Sourced from application.properties -> env var. Never hardcode this.
    // Defaults to empty so the app still starts locally without configuration;
    // see getSignKey() for what happens then.
    @Value("${jwt.secret:}")
    private String secretKey;

    @Value("${jwt.expiration-ms:86400000}")
    private long expirationMs;

    /**
     * Lazily-created fallback key, used only when no jwt.secret is configured.
     * Random per process on purpose: a hardcoded fallback would be a shared
     * signing key that anyone reading the source could use to mint admin
     * tokens. The cost is that restarting logs everyone out, which is
     * acceptable in development and loudly warned about below.
     */
    private volatile Key generatedDevKey;

    /**
     * Derives a signing key that is always at least 256 bits (required by
     * HS256). Prefers a base64-encoded secret that already decodes to >= 32
     * bytes. If the configured value is a plain string (or base64 but too
     * short), falls back to a SHA-256 hash of the raw bytes, which is always
     * exactly 32 bytes.
     */
    private Key getSignKey() {
        // No secret configured at all -> fall back to a per-process random key
        // so the application still boots for local development.
        if (secretKey == null || secretKey.isBlank()) {
            return devKey();
        }

        byte[] keyBytes = null;
        try {
            byte[] decoded = Decoders.BASE64.decode(secretKey);
            if (decoded.length >= 32) {
                keyBytes = decoded;
            }
        } catch (IllegalArgumentException ex) {
            // Not valid base64 -> treat as a plain secret below.
        }

        if (keyBytes == null) {
            // Plain string (or base64 that was too short): hash it to get a
            // key of exactly the 32 bytes HS256 requires.
            keyBytes = sha256(secretKey);
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Key devKey() {
        Key key = generatedDevKey;
        if (key == null) {
            synchronized (this) {
                key = generatedDevKey;
                if (key == null) {
                    key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
                    generatedDevKey = key;
                    log.warn("=================================================================");
                    log.warn("jwt.secret is not set - generated a TEMPORARY random signing key.");
                    log.warn("The application will run, but every restart logs all users out.");
                    log.warn("Set the JWT_SECRET environment variable before deploying:");
                    log.warn("    openssl rand -base64 48");
                    log.warn("=================================================================");
                }
            }
        }
        return key;
    }

    private byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public String generateToken(String email) {

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return resolver.apply(claims);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {

        String username = extractUsername(token);

        return username.equals(userDetails.getUsername());
    }
}