package com.etour.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.etour.dto.NewsletterRequest;
import com.etour.dto.NewsletterResponse;
import com.etour.service.NewsletterService;

import jakarta.validation.Valid;

/**
 * Newsletter signup. Subscribe/unsubscribe are public (a visitor shouldn't
 * need an account to join a mailing list); listing subscribers is admin-only.
 */
@RestController
@RequestMapping("/api/newsletter")
public class NewsletterController {

    private final NewsletterService newsletterService;

    public NewsletterController(NewsletterService newsletterService) {
        this.newsletterService = newsletterService;
    }

    @PostMapping("/subscribe")
    public ResponseEntity<NewsletterResponse> subscribe(@Valid @RequestBody NewsletterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(newsletterService.subscribe(request));
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<Void> unsubscribe(@Valid @RequestBody NewsletterRequest request) {
        newsletterService.unsubscribe(request.getEmail());
        return ResponseEntity.noContent().build();
    }

    // ----- Admin -----

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<NewsletterResponse>> list(
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "subscribedAt"));
        return ResponseEntity.ok(newsletterService.list(active, pageable));
    }

    @GetMapping("/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> activeCount() {
        return ResponseEntity.ok(newsletterService.activeCount());
    }

    @DeleteMapping("/{subscriberId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long subscriberId) {
        newsletterService.delete(subscriberId);
        return ResponseEntity.noContent().build();
    }
}
