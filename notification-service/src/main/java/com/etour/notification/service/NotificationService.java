package com.etour.notification.service;

import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.etour.notification.config.NotificationProperties;
import com.etour.notification.domain.Notification;
import com.etour.notification.domain.NotificationAttachment;
import com.etour.notification.domain.NotificationStatus;
import com.etour.notification.domain.NotificationTemplate;
import com.etour.notification.dto.NotificationDtos.AttachmentInput;
import com.etour.notification.dto.NotificationDtos.AttachmentSummary;
import com.etour.notification.dto.NotificationDtos.NotificationResponse;
import com.etour.notification.dto.NotificationDtos.SendRequest;
import com.etour.notification.repository.NotificationRepository;
import com.etour.notification.repository.NotificationTemplateRepository;
import com.etour.notification.web.NotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/** Enqueue, query and re-queue. Actual delivery is the worker's job. */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notifications;
    private final NotificationTemplateRepository templates;
    private final TemplateRenderer renderer;
    private final NotificationProperties properties;
    private final ObjectMapper objectMapper;

    public NotificationService(NotificationRepository notifications,
                               NotificationTemplateRepository templates,
                               TemplateRenderer renderer,
                               NotificationProperties properties,
                               ObjectMapper objectMapper) {
        this.notifications = notifications;
        this.templates = templates;
        this.renderer = renderer;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    /**
     * Queues a message. Renders the template up front so the stored subject/body
     * are what will actually be delivered - a template edited later cannot change
     * the content of a message that is already queued or sent.
     */
    @Transactional
    public Notification enqueue(SendRequest request) {

        // Idempotency is checked before doing any work, but the unique index is
        // what actually guarantees it - two concurrent requests with the same key
        // both pass this check, and the loser is caught below.
        if (request.getIdempotencyKey() != null && !request.getIdempotencyKey().isBlank()) {
            Optional<Notification> existing = notifications.findByIdempotencyKey(request.getIdempotencyKey());
            if (existing.isPresent()) {
                log.info("Idempotent replay of key {} -> notification {}",
                        request.getIdempotencyKey(), existing.get().getNotificationId());
                return existing.get();
            }
        }

        NotificationTemplate template = templates.findByCode(request.getTemplate())
                .orElseThrow(() -> new NotFoundException("Unknown template: " + request.getTemplate()));

        Map<String, String> variables = request.getVariables() == null ? Map.of() : request.getVariables();

        Notification notification = new Notification();
        notification.setRecipient(request.getRecipient().trim());
        notification.setTemplateCode(template.getCode());
        notification.setSubject(renderer.render(template.getSubject(), variables));
        notification.setBody(renderer.render(template.getBody(), variables));
        notification.setVariablesJson(toJson(variables));
        notification.setStatus(NotificationStatus.PENDING);
        notification.setAttempts(0);
        // Captured per row, so a later policy change cannot revive rows already
        // dead-lettered under the old limit.
        notification.setMaxAttempts(Math.max(1, properties.getMaxAttempts()));
        notification.setNextAttemptAt(Instant.now());
        notification.setIdempotencyKey(blankToNull(request.getIdempotencyKey()));
        attachAll(notification, request);

        try {
            Notification saved = notifications.save(notification);
            log.info("Queued notification {} to {} using template {}",
                    saved.getNotificationId(), saved.getRecipient(), saved.getTemplateCode());
            return saved;
        } catch (DataIntegrityViolationException ex) {
            // Lost the race on the unique idempotency index. The other request
            // queued the same message, so returning its row is the correct
            // answer - and still exactly one message goes out.
            return notifications.findByIdempotencyKey(request.getIdempotencyKey())
                    .orElseThrow(() -> ex);
        }
    }

    @Transactional(readOnly = true)
    public Page<Notification> list(NotificationStatus status, Pageable pageable) {
        return status == null
                ? notifications.findAllByOrderByCreatedAtDesc(pageable)
                : notifications.findByStatusOrderByCreatedAtDesc(status, pageable);
    }

    /**
     * Decodes and attaches the request's files, rejecting anything oversized
     * before it can be queued.
     */
    private void attachAll(Notification notification, SendRequest request) {
        List<AttachmentInput> inputs = request.getAttachments();
        if (inputs == null || inputs.isEmpty()) {
            return;
        }

        long total = 0;
        for (AttachmentInput input : inputs) {
            byte[] bytes;
            try {
                bytes = Base64.getDecoder().decode(input.getContentBase64());
            } catch (IllegalArgumentException ex) {
                // A 400 with the filename, rather than a queued message that can
                // only ever fail on delivery.
                throw new IllegalArgumentException(
                        "Attachment '" + input.getFilename() + "' is not valid base64");
            }

            total += bytes.length;
            if (total > properties.getMaxAttachmentBytes()) {
                throw new IllegalArgumentException(
                        "Attachments exceed the " + properties.getMaxAttachmentBytes()
                                + " byte limit for a single message");
            }

            String contentType = input.getContentType() == null || input.getContentType().isBlank()
                    ? "application/octet-stream"
                    : input.getContentType();

            notification.addAttachment(
                    new NotificationAttachment(notification, input.getFilename(), contentType, bytes));
        }
    }

    @Transactional(readOnly = true)
    public Notification get(long id) {
        return notifications.findById(id)
                .orElseThrow(() -> new NotFoundException("Notification not found: " + id));
    }

    /**
     * The single-message view, built inside the transaction so the lazy
     * attachment collection can be read. The list endpoint deliberately does not
     * do this - see NotificationAttachment.
     */
    @Transactional(readOnly = true)
    public NotificationResponse getDetail(long id) {
        Notification notification = get(id);
        NotificationResponse response = NotificationResponse.from(notification);
        response.setAttachments(notification.getAttachments().stream()
                .map(a -> new AttachmentSummary(a.getFilename(), a.getContentType(), a.getSizeBytes()))
                .toList());
        return response;
    }

    @Transactional(readOnly = true)
    public long countByStatus(NotificationStatus status) {
        return notifications.countByStatus(status);
    }

    /**
     * Puts a dead letter back on the queue with a fresh attempt budget.
     *
     * <p>This is the manual escape hatch that makes a one-retry policy workable:
     * the service stops trying by itself after two attempts, and a human decides
     * whether the underlying problem (wrong address, mail server down) is fixed.
     */
    @Transactional
    public Notification requeue(long id) {
        Notification notification = get(id);

        if (notification.getStatus() != NotificationStatus.FAILED) {
            throw new IllegalStateException(
                    "Only a failed notification can be retried; notification " + id
                            + " is " + notification.getStatus());
        }

        notification.setStatus(NotificationStatus.PENDING);
        notification.setAttempts(0);
        notification.setMaxAttempts(Math.max(1, properties.getMaxAttempts()));
        notification.setNextAttemptAt(Instant.now());
        notification.setLastError(null);
        notification.setUpdatedAt(Instant.now());

        log.info("Notification {} manually re-queued by an admin", id);
        return notifications.save(notification);
    }

    private String toJson(Map<String, String> variables) {
        try {
            return objectMapper.writeValueAsString(variables);
        } catch (JsonProcessingException ex) {
            // The variables are a debugging aid, not part of delivery - losing
            // them must never stop a message going out.
            log.warn("Could not serialize notification variables: {}", ex.getMessage());
            return null;
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
