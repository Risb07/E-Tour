package com.etour.notification.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * One queued outbound message and its delivery history.
 *
 * <p>The rendered subject/body are stored alongside the variables that produced
 * them. That is deliberate duplication: a template edited after the fact must
 * never change what an already-sent message says it sent, so the audit trail
 * has to hold the text as it actually went out.
 */
@Entity
@Table(name = "notification",
        uniqueConstraints = @UniqueConstraint(name = "uk_notification_idempotency", columnNames = "idempotency_key"),
        indexes = {
                // The worker's claim query filters on exactly these two columns.
                @Index(name = "ix_notification_due", columnList = "status,next_attempt_at"),
                @Index(name = "ix_notification_created", columnList = "created_at")
        })
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @Column(name = "recipient", nullable = false, length = 320)
    private String recipient;

    @Column(name = "template_code", nullable = false, length = 64)
    private String templateCode;

    @Column(name = "subject", nullable = false, length = 500)
    private String subject;

    // NOT @Lob. Hibernate 6 maps a @Lob String onto MySQL's *tinytext* - 255
    // bytes - unless a length is given, which silently truncates any real email
    // body. An explicit length maps to `text` (64KB) instead, which is ample
    // here and portable. H2 does not enforce tinytext's width, so this is a
    // difference only a real MySQL run reveals.
    @Column(name = "body", nullable = false, length = 65535)
    private String body;

    /** The variables the template was rendered with, as JSON. Kept for support questions. */
    @Column(name = "variables_json", length = 65535)
    private String variablesJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private NotificationStatus status = NotificationStatus.PENDING;

    /** Attempts already made. Compared against maxAttempts to decide retry vs dead-letter. */
    @Column(name = "attempts", nullable = false)
    private int attempts = 0;

    /**
     * Total attempts allowed, captured per row at enqueue time rather than read
     * from config at send time - so changing the policy later cannot retroactively
     * revive rows that were already dead-lettered under the old one.
     */
    @Column(name = "max_attempts", nullable = false)
    private int maxAttempts = 2;

    /** When the worker may next touch this row. Null once terminal. */
    @Column(name = "next_attempt_at")
    private Instant nextAttemptAt;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Column(name = "sent_at")
    private Instant sentAt;

    /**
     * Caller-supplied de-duplication key. A repeat enqueue with the same key
     * returns the existing row instead of queueing a second copy - so a client
     * retrying a failed HTTP call cannot double-send the email.
     */
    @Column(name = "idempotency_key", length = 200)
    private String idempotencyKey;

    /** Free-text note of which transport handled it ("smtp" / "simulated"). */
    @Column(name = "transport", length = 20)
    private String transport;

    /**
     * Lazy: the admin screen lists this table constantly and must never pull
     * receipt PDFs it is not going to show. Only the transport dereferences it,
     * and it does so inside the dispatcher's transaction.
     */
    @OneToMany(mappedBy = "notification", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<NotificationAttachment> attachments = new ArrayList<>();

    public List<NotificationAttachment> getAttachments() { return attachments; }
    public void setAttachments(List<NotificationAttachment> attachments) { this.attachments = attachments; }

    public void addAttachment(NotificationAttachment attachment) {
        attachment.setNotification(this);
        this.attachments.add(attachment);
    }

    public Long getNotificationId() { return notificationId; }
    public void setNotificationId(Long notificationId) { this.notificationId = notificationId; }

    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }

    public String getTemplateCode() { return templateCode; }
    public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getVariablesJson() { return variablesJson; }
    public void setVariablesJson(String variablesJson) { this.variablesJson = variablesJson; }

    public NotificationStatus getStatus() { return status; }
    public void setStatus(NotificationStatus status) { this.status = status; }

    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }

    public int getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }

    public Instant getNextAttemptAt() { return nextAttemptAt; }
    public void setNextAttemptAt(Instant nextAttemptAt) { this.nextAttemptAt = nextAttemptAt; }

    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public Instant getSentAt() { return sentAt; }
    public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public String getTransport() { return transport; }
    public void setTransport(String transport) { this.transport = transport; }

    /** True when another attempt is still permitted after the one just recorded. */
    public boolean hasAttemptsLeft() {
        return attempts < maxAttempts;
    }
}
