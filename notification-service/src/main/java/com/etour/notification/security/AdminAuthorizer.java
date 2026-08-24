package com.etour.notification.security;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Decides whether a caller is an eTour ADMIN.
 *
 * <p>It has to ask, because an eTour JWT carries no role claim - both backends
 * re-read the role from the database on every request. This service has no
 * access to that database (it owns a different one), so instead it asks
 * whichever backend is currently live, using the caller's own token, against a
 * route that is ADMIN-only on <b>both</b> implementations:
 * {@code GET /api/admin/dashboard} (Java: {@code /api/admin/**} requires
 * ADMIN in SecurityConfig; .NET: {@code [Authorize(Roles = "ADMIN")]}).
 *
 * <p>200 means admin, 401/403 means not. That keeps the authorisation decision
 * with the system that owns user data, needs no database credentials here, and
 * required no change to either backend - which is the constraint this whole
 * service was built under.
 *
 * <p>The trade-off, stated plainly: admin endpoints on this service depend on a
 * backend being reachable. Delivery does not - the queue keeps draining either
 * way - and only positive results are cached, so revoking someone's admin role
 * takes effect within the TTL rather than being remembered indefinitely.
 */
@Component
public class AdminAuthorizer {

    private static final Logger log = LoggerFactory.getLogger(AdminAuthorizer.class);

    private final RestClient restClient;
    private final String probePath;
    private final Duration cacheTtl;

    /** token -> when its positive result expires. Negative results are never cached. */
    private final Map<String, Instant> adminUntil = new ConcurrentHashMap<>();

    public AdminAuthorizer(RestClient.Builder builder,
                           @Value("${security.backend.base-url:http://backend:8080}") String baseUrl,
                           @Value("${security.backend.admin-probe-path:/api/admin/dashboard}") String probePath,
                           @Value("${security.backend.cache-ttl-seconds:60}") long cacheTtlSeconds) {
        this.restClient = builder.baseUrl(baseUrl).build();
        this.probePath = probePath;
        this.cacheTtl = Duration.ofSeconds(cacheTtlSeconds);
    }

    public boolean isAdmin(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        Instant cachedUntil = adminUntil.get(token);
        if (cachedUntil != null) {
            if (Instant.now().isBefore(cachedUntil)) {
                return true;
            }
            adminUntil.remove(token);
        }

        boolean admin = probeBackend(token);
        if (admin) {
            evictExpired();
            adminUntil.put(token, Instant.now().plus(cacheTtl));
        }
        return admin;
    }

    private boolean probeBackend(String token) {
        try {
            HttpStatus status = restClient.get()
                    .uri(probePath)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .exchange((request, response) -> HttpStatus.valueOf(response.getStatusCode().value()));

            if (status == HttpStatus.OK) {
                return true;
            }

            if (status == HttpStatus.UNAUTHORIZED || status == HttpStatus.FORBIDDEN) {
                return false;
            }

            // Anything else is the backend misbehaving rather than a verdict on
            // this caller, so deny without caching and say so in the log.
            log.warn("Admin probe returned an unexpected {} - denying access", status);
            return false;

        } catch (Exception ex) {
            log.warn("Could not reach the backend to check admin role ({}): {}",
                    probePath, ex.getMessage());
            return false;
        }
    }

    /**
     * Bounded cleanup so a long-running process cannot accumulate one entry per
     * token ever seen. Cheap because the map only holds admins, and only briefly.
     */
    private void evictExpired() {
        Instant now = Instant.now();
        adminUntil.entrySet().removeIf(entry -> now.isAfter(entry.getValue()));
    }
}
