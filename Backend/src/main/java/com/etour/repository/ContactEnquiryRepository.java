package com.etour.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.etour.entity.ContactEnquiry;
import com.etour.enums.EnquiryStatus;

public interface ContactEnquiryRepository extends JpaRepository<ContactEnquiry, Long> {

    /** Admin: filter the inbox by state. */
    Page<ContactEnquiry> findByStatus(EnquiryStatus status, Pageable pageable);

    /**
     * Admin free-text search across the fields an agent would actually look
     * in. Derived rather than a @Query so it stays parameterised - the search
     * term is bound, never concatenated.
     */
    Page<ContactEnquiry> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrSubjectContainingIgnoreCase(
            String name, String email, String subject, Pageable pageable);

    Page<ContactEnquiry> findByStatusAndNameContainingIgnoreCaseOrStatusAndEmailContainingIgnoreCaseOrStatusAndSubjectContainingIgnoreCase(
            EnquiryStatus s1, String name,
            EnquiryStatus s2, String email,
            EnquiryStatus s3, String subject,
            Pageable pageable);

    long countByStatus(EnquiryStatus status);
}
