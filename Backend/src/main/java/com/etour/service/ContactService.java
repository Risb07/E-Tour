package com.etour.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.etour.dto.ContactEnquiryRequest;
import com.etour.dto.ContactEnquiryResponse;
import com.etour.enums.EnquiryStatus;

public interface ContactService {

    /** Public: store an enquiry from the Contact page. Always arrives as NEW. */
    ContactEnquiryResponse submit(ContactEnquiryRequest request);

    /**
     * Admin: paginated inbox, optionally narrowed by state and/or a free-text
     * term matched against name, email and subject.
     */
    Page<ContactEnquiryResponse> list(EnquiryStatus status, String search, Pageable pageable);

    /** Admin: read one enquiry in full. */
    ContactEnquiryResponse getById(Long enquiryId);

    /** Admin: move an enquiry along - typically NEW to RESOLVED. */
    ContactEnquiryResponse updateStatus(Long enquiryId, EnquiryStatus status);

    /** Admin: hard delete (spam, or an erasure request from the sender). */
    void delete(Long enquiryId);

    /** Admin: unread count, for a dashboard badge. */
    long countByStatus(EnquiryStatus status);
}
