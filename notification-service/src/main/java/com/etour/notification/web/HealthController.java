package com.etour.notification.web;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Liveness probe.
 *
 * <p>Its own controller, and its own path rather than {@code /svc/notifications/health},
 * so it can never be mistaken for a notification id by the {@code /{id}} route.
 * Unauthenticated on purpose: compose's HEALTHCHECK has no eTour token to present,
 * and this reveals nothing beyond "the process is up".
 */
@RestController
public class HealthController {

    @GetMapping("/svc/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "etour-notification-service"));
    }
}
