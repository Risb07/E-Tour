package com.etour.service.impl;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.etour.dto.ContactEnquiryRequest;
import com.etour.dto.ContactEnquiryResponse;
import com.etour.entity.ContactEnquiry;
import com.etour.enums.EnquiryStatus;
import com.etour.exception.ResourceNotFoundException;
import com.etour.repository.ContactEnquiryRepository;
import com.etour.service.ContactService;

@Service
public class ContactServiceImpl implements ContactService {

    private final ContactEnquiryRepository repository;

    public ContactServiceImpl(ContactEnquiryRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public ContactEnquiryResponse submit(ContactEnquiryRequest request) {
        ContactEnquiry enquiry = new ContactEnquiry();
        enquiry.setName(trim(request.getName()));
        enquiry.setEmail(normaliseEmail(request.getEmail()));
        // An empty phone field is stored as null rather than "", so "has a
        // phone number" is a single check in the admin view.
        enquiry.setPhone(blankToNull(request.getPhone()));
        enquiry.setSubject(trim(request.getSubject()));
        enquiry.setMessage(trim(request.getMessage()));

        // Set here, never taken from the request - see ContactEnquiryRequest.
        enquiry.setStatus(EnquiryStatus.NEW);
        enquiry.setCreatedAt(LocalDateTime.now());

        return toDto(repository.save(enquiry));
    }

    @Override
    public Page<ContactEnquiryResponse> list(EnquiryStatus status, String search, Pageable pageable) {
        String term = blankToNull(search);
        Page<ContactEnquiry> page;

        if (status == null && term == null) {
            page = repository.findAll(pageable);
        } else if (term == null) {
            page = repository.findByStatus(status, pageable);
        } else if (status == null) {
            page = repository
                    .findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrSubjectContainingIgnoreCase(
                            term, term, term, pageable);
        } else {
            // The derived name repeats the status for each OR branch so the
            // filter applies to every matched field, not just the first.
            page = repository
                    .findByStatusAndNameContainingIgnoreCaseOrStatusAndEmailContainingIgnoreCaseOrStatusAndSubjectContainingIgnoreCase(
                            status, term, status, term, status, term, pageable);
        }
        return page.map(this::toDto);
    }

    @Override
    public ContactEnquiryResponse getById(Long enquiryId) {
        return toDto(require(enquiryId));
    }

    @Override
    @Transactional
    public ContactEnquiryResponse updateStatus(Long enquiryId, EnquiryStatus status) {
        ContactEnquiry enquiry = require(enquiryId);
        enquiry.setStatus(status);
        enquiry.setUpdatedAt(LocalDateTime.now());
        return toDto(repository.save(enquiry));
    }

    @Override
    @Transactional
    public void delete(Long enquiryId) {
        repository.delete(require(enquiryId));
    }

    @Override
    public long countByStatus(EnquiryStatus status) {
        return repository.countByStatus(status);
    }

    private ContactEnquiry require(Long enquiryId) {
        return repository.findById(enquiryId)
                .orElseThrow(() -> new ResourceNotFoundException("Enquiry not found"));
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String blankToNull(String value) {
        String trimmed = trim(value);
        return trimmed == null || trimmed.isEmpty() ? null : trimmed;
    }

    private String normaliseEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private ContactEnquiryResponse toDto(ContactEnquiry e) {
        return new ContactEnquiryResponse(
                e.getEnquiryId(), e.getName(), e.getEmail(), e.getPhone(), e.getSubject(),
                e.getMessage(), e.getStatus().name(), e.getCreatedAt(), e.getUpdatedAt());
    }
}
