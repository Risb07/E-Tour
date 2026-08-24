package com.etour.notification.web;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.etour.notification.domain.Notification;
import com.etour.notification.domain.NotificationStatus;
import com.etour.notification.dto.NotificationDtos.NotificationResponse;
import com.etour.notification.dto.NotificationDtos.SendRequest;
import com.etour.notification.dto.NotificationDtos.StatsResponse;
import com.etour.notification.dto.NotificationDtos.TemplateResponse;
import com.etour.notification.repository.NotificationTemplateRepository;
import com.etour.notification.security.CallerContext;
import com.etour.notification.service.NotificationService;

import jakarta.validation.Valid;

/**
 * The notification API, served at {@code /svc/notifications} through nginx.
 *
 * <p>The {@code /svc} prefix rather than {@code /api} is deliberate: nginx routes
 * all of {@code /api/} to whichever eTour backend is active, so anything under it
 * would either collide or depend on nginx's longest-prefix matching to keep
 * working. A separate prefix means this service can never shadow a backend route,
 * now or after either backend adds endpoints.
 */
@RestController
@RequestMapping("/svc/notifications")
public class NotificationController {

    private static final int MAX_PAGE_SIZE = 100;

    private final NotificationService service;
    private final NotificationTemplateRepository templates;
    private final CallerContext caller;

    public NotificationController(NotificationService service,
                                  NotificationTemplateRepository templates,
                                  CallerContext caller) {
        this.service = service;
        this.templates = templates;
        this.caller = caller;
    }

    /**
     * Queues a message. 202, not 201: the row exists but nothing has been
     * delivered yet, and the caller should not read the response as "sent".
     */
    @PostMapping
    public ResponseEntity<NotificationResponse> send(@Valid @RequestBody SendRequest request) {
        caller.requireAuthenticated();
        Notification queued = service.enqueue(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(NotificationResponse.from(queued));
    }

    /** Admin: the delivery log, newest first, optionally filtered by status. */
    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> list(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {

        caller.requireAdmin();

        NotificationStatus filter = parseStatus(status);
        // Clamped so a caller cannot ask for the whole table in one request.
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

        Page<Notification> result = service.list(filter, PageRequest.of(Math.max(page, 0), safeSize));
        return ResponseEntity.ok(result.map(NotificationResponse::from));
    }

    /** Admin: counts per status, for the summary cards. */
    @GetMapping("/stats")
    public ResponseEntity<StatsResponse> stats() {
        caller.requireAdmin();
        return ResponseEntity.ok(new StatsResponse(
                service.countByStatus(NotificationStatus.PENDING),
                service.countByStatus(NotificationStatus.SENT),
                service.countByStatus(NotificationStatus.FAILED)));
    }

    /** Admin: the available templates. */
    @GetMapping("/templates")
    public ResponseEntity<List<TemplateResponse>> templates() {
        caller.requireAdmin();
        List<TemplateResponse> all = templates.findAll().stream()
                .map(t -> new TemplateResponse(t.getCode(), t.getDescription(), t.getSubject(), t.getBody()))
                .toList();
        return ResponseEntity.ok(all);
    }

    /** Detail view - the only place attachment metadata is returned. */
    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> get(@PathVariable("id") long id) {
        caller.requireAdmin();
        return ResponseEntity.ok(service.getDetail(id));
    }

    /**
     * Admin: put a dead letter back on the queue. This is the counterpart to the
     * one-retry policy - the service stops on its own, a human restarts it.
     */
    @PostMapping("/{id}/retry")
    public ResponseEntity<NotificationResponse> retry(@PathVariable("id") long id) {
        caller.requireAdmin();
        return ResponseEntity.ok(NotificationResponse.from(service.requeue(id)));
    }

    private static NotificationStatus parseStatus(String status) {
        if (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status)) {
            return null;
        }
        try {
            return NotificationStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Unknown status '" + status + "'. Expected one of: PENDING, SENT, FAILED, ALL");
        }
    }
}
