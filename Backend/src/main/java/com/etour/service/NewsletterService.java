package com.etour.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.etour.dto.NewsletterRequest;
import com.etour.dto.NewsletterResponse;

public interface NewsletterService {

    /** Public signup. Idempotent: re-subscribing an existing address reactivates it. */
    NewsletterResponse subscribe(NewsletterRequest request);

    /** Public unsubscribe by email. Silently succeeds for unknown addresses. */
    void unsubscribe(String email);

    /** Admin: paginated list, optionally filtered by active state. */
    Page<NewsletterResponse> list(Boolean active, Pageable pageable);

    /** Admin: hard delete (GDPR erasure requests). */
    void delete(Long subscriberId);

    long activeCount();
}
