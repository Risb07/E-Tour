package com.etour.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.etour.dto.ContactEnquiryRequest;
import com.etour.dto.ContactEnquiryResponse;
import com.etour.enums.EnquiryStatus;
import com.etour.service.ContactService;

import jakarta.validation.Valid;

/**
 * Contact enquiries. Submitting is public - a visitor shouldn't need an
 * account to ask a question - while reading, filtering, closing and deleting
 * the inbox is admin-only. Mirrors how NewsletterController splits the same
 * public-write / admin-read concern.
 */
@RestController
@RequestMapping("/api/contact")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    /** Public: submit an enquiry from the Contact page. */
    @PostMapping
    public ResponseEntity<ContactEnquiryResponse> submit(@Valid @RequestBody ContactEnquiryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contactService.submit(request));
    }

    // ----- Admin -----

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ContactEnquiryResponse>> list(
            @RequestParam(required = false) EnquiryStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {

        // Newest first - an enquiry inbox is read from the top.
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(contactService.list(status, search, pageable));
    }

    @GetMapping("/{enquiryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ContactEnquiryResponse> getById(@PathVariable Long enquiryId) {
        return ResponseEntity.ok(contactService.getById(enquiryId));
    }

    /** Admin: mark an enquiry as IN_PROGRESS or RESOLVED. */
    @PatchMapping("/{enquiryId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ContactEnquiryResponse> updateStatus(@PathVariable Long enquiryId,
            @RequestParam EnquiryStatus status) {
        return ResponseEntity.ok(contactService.updateStatus(enquiryId, status));
    }

    @GetMapping("/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> countNew() {
        return ResponseEntity.ok(contactService.countByStatus(EnquiryStatus.NEW));
    }

    @DeleteMapping("/{enquiryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long enquiryId) {
        contactService.delete(enquiryId);
        return ResponseEntity.noContent().build();
    }
}
