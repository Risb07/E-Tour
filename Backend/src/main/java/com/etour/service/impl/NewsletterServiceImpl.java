package com.etour.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.etour.dto.NewsletterRequest;
import com.etour.dto.NewsletterResponse;
import com.etour.entity.NewsletterSubscriber;
import com.etour.exception.ResourceNotFoundException;
import com.etour.repository.NewsletterSubscriberRepository;
import com.etour.service.NewsletterService;

@Service
public class NewsletterServiceImpl implements NewsletterService {

    private final NewsletterSubscriberRepository repository;

    public NewsletterServiceImpl(NewsletterSubscriberRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public NewsletterResponse subscribe(NewsletterRequest request) {
        String email = normalise(request.getEmail());

        // Duplicate prevention: re-subscribing is an update, not an insert.
        // Returning success (rather than a 409) is deliberate - telling an
        // anonymous caller "that address is already subscribed" leaks whether
        // a given person is on the list.
        Optional<NewsletterSubscriber> existing = repository.findByEmailIgnoreCase(email);
        if (existing.isPresent()) {
            NewsletterSubscriber subscriber = existing.get();
            if (Boolean.FALSE.equals(subscriber.getActive())) {
                subscriber.setActive(true);
                subscriber.setUnsubscribedAt(null);
                subscriber.setSubscribedAt(LocalDateTime.now());
            }
            if (request.getName() != null && !request.getName().isBlank()) {
                subscriber.setName(request.getName().trim());
            }
            return toDto(repository.save(subscriber));
        }

        NewsletterSubscriber subscriber = new NewsletterSubscriber();
        subscriber.setEmail(email);
        subscriber.setName(request.getName() == null ? null : request.getName().trim());
        subscriber.setActive(true);
        subscriber.setSubscribedAt(LocalDateTime.now());
        return toDto(repository.save(subscriber));
    }

    @Override
    @Transactional
    public void unsubscribe(String email) {
        // No 404 for an unknown address - same reasoning as above, an
        // anonymous caller shouldn't be able to probe the subscriber list.
        repository.findByEmailIgnoreCase(normalise(email)).ifPresent(subscriber -> {
            subscriber.setActive(false);
            subscriber.setUnsubscribedAt(LocalDateTime.now());
            repository.save(subscriber);
        });
    }

    @Override
    public Page<NewsletterResponse> list(Boolean active, Pageable pageable) {
        Page<NewsletterSubscriber> page = (active == null)
                ? repository.findAll(pageable)
                : repository.findByActive(active, pageable);
        return page.map(this::toDto);
    }

    @Override
    @Transactional
    public void delete(Long subscriberId) {
        NewsletterSubscriber subscriber = repository.findById(subscriberId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscriber not found"));
        repository.delete(subscriber);
    }

    @Override
    public long activeCount() {
        return repository.countByActiveTrue();
    }

    private String normalise(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private NewsletterResponse toDto(NewsletterSubscriber s) {
        return new NewsletterResponse(s.getSubscriberId(), s.getEmail(), s.getName(),
                s.getActive(), s.getSubscribedAt(), s.getUnsubscribedAt());
    }
}
