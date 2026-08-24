package com.etour.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.etour.entity.NewsletterSubscriber;

public interface NewsletterSubscriberRepository extends JpaRepository<NewsletterSubscriber, Long> {

    // Case-insensitive so "A@b.com" and "a@b.com" are the same subscriber -
    // emails are stored lower-cased by the service, this guards older rows.
    Optional<NewsletterSubscriber> findByEmailIgnoreCase(String email);

    Page<NewsletterSubscriber> findByActive(Boolean active, Pageable pageable);

    long countByActiveTrue();
}
