package com.etour.notification.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.etour.notification.domain.Notification;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Request/response shapes for the notification API. */
public final class NotificationDtos {

    private NotificationDtos() {
    }

    /** Enqueue request. */
    public static class SendRequest {

        @NotBlank(message = "recipient is required")
        @Email(message = "recipient must be a valid email address")
        @Size(max = 320, message = "recipient must be at most 320 characters")
        private String recipient;

        @NotBlank(message = "template is required")
        @Size(max = 64)
        private String template;

        /** Values for the template's {{placeholders}}. Missing keys render as blank. */
        private Map<String, String> variables;

        /**
         * Optional de-duplication key. Re-sending the same key returns the
         * original notification rather than queueing a second copy.
         */
        @Size(max = 200)
        private String idempotencyKey;

        /** Files to attach - in practice the booking receipt PDF. */
        @Valid
        private List<AttachmentInput> attachments;

        public String getRecipient() { return recipient; }
        public void setRecipient(String recipient) { this.recipient = recipient; }

        public String getTemplate() { return template; }
        public void setTemplate(String template) { this.template = template; }

        public Map<String, String> getVariables() { return variables; }
        public void setVariables(Map<String, String> variables) { this.variables = variables; }

        public String getIdempotencyKey() { return idempotencyKey; }
        public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

        public List<AttachmentInput> getAttachments() { return attachments; }
        public void setAttachments(List<AttachmentInput> attachments) { this.attachments = attachments; }
    }

    /**
     * An attachment on the way in. Base64 because this is a JSON API - the
     * caller is a backend service posting a PDF it just generated, not a
     * browser doing a multipart upload.
     */
    public static class AttachmentInput {

        @NotBlank(message = "attachment filename is required")
        @Size(max = 255)
        private String filename;

        @Size(max = 120)
        private String contentType;

        @NotBlank(message = "attachment content is required")
        private String contentBase64;

        public String getFilename() { return filename; }
        public void setFilename(String filename) { this.filename = filename; }

        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }

        public String getContentBase64() { return contentBase64; }
        public void setContentBase64(String contentBase64) { this.contentBase64 = contentBase64; }
    }

    /** Attachment metadata on the way out - never the bytes. */
    public static class AttachmentSummary {

        private final String filename;
        private final String contentType;
        private final long sizeBytes;

        public AttachmentSummary(String filename, String contentType, long sizeBytes) {
            this.filename = filename;
            this.contentType = contentType;
            this.sizeBytes = sizeBytes;
        }

        public String getFilename() { return filename; }
        public String getContentType() { return contentType; }
        public long getSizeBytes() { return sizeBytes; }
    }

    /** What the API returns for a single notification. */
    public static class NotificationResponse {

        private Long notificationId;
        private String recipient;
        private String templateCode;
        private String subject;
        private String body;
        private String status;
        private int attempts;
        private int maxAttempts;
        private String lastError;
        private String transport;
        private Instant createdAt;
        private Instant nextAttemptAt;
        private Instant sentAt;
        /** True when every allowed attempt failed - i.e. this row is a dead letter. */
        private boolean deadLettered;

        /**
         * Populated only by the single-message endpoint, which loads them inside
         * its transaction. Left empty by the list endpoint on purpose - see
         * NotificationAttachment for why the log must not drag PDFs around.
         */
        private List<AttachmentSummary> attachments = List.of();

        public static NotificationResponse from(Notification n) {
            NotificationResponse r = new NotificationResponse();
            r.notificationId = n.getNotificationId();
            r.recipient = n.getRecipient();
            r.templateCode = n.getTemplateCode();
            r.subject = n.getSubject();
            r.body = n.getBody();
            r.status = n.getStatus().name();
            r.attempts = n.getAttempts();
            r.maxAttempts = n.getMaxAttempts();
            r.lastError = n.getLastError();
            r.transport = n.getTransport();
            r.createdAt = n.getCreatedAt();
            r.nextAttemptAt = n.getNextAttemptAt();
            r.sentAt = n.getSentAt();
            r.deadLettered = n.getStatus() == com.etour.notification.domain.NotificationStatus.FAILED;
            return r;
        }

        public Long getNotificationId() { return notificationId; }
        public String getRecipient() { return recipient; }
        public String getTemplateCode() { return templateCode; }
        public String getSubject() { return subject; }
        public String getBody() { return body; }
        public String getStatus() { return status; }
        public int getAttempts() { return attempts; }
        public int getMaxAttempts() { return maxAttempts; }
        public String getLastError() { return lastError; }
        public String getTransport() { return transport; }
        public Instant getCreatedAt() { return createdAt; }
        public Instant getNextAttemptAt() { return nextAttemptAt; }
        public Instant getSentAt() { return sentAt; }
        public boolean isDeadLettered() { return deadLettered; }

        public List<AttachmentSummary> getAttachments() { return attachments; }
        public void setAttachments(List<AttachmentSummary> attachments) { this.attachments = attachments; }
    }

    /** Counts per status, for the admin screen's summary cards. */
    public static class StatsResponse {

        private final long pending;
        private final long sent;
        private final long failed;
        private final long total;

        public StatsResponse(long pending, long sent, long failed) {
            this.pending = pending;
            this.sent = sent;
            this.failed = failed;
            this.total = pending + sent + failed;
        }

        public long getPending() { return pending; }
        public long getSent() { return sent; }
        public long getFailed() { return failed; }
        public long getTotal() { return total; }
    }

    /** A template as listed by the API. */
    public static class TemplateResponse {

        private final String code;
        private final String description;
        private final String subject;
        private final String body;

        public TemplateResponse(String code, String description, String subject, String body) {
            this.code = code;
            this.description = description;
            this.subject = subject;
            this.body = body;
        }

        public String getCode() { return code; }
        public String getDescription() { return description; }
        public String getSubject() { return subject; }
        public String getBody() { return body; }
    }
}
