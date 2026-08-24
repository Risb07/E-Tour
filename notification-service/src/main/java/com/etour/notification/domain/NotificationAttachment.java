package com.etour.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * A file attached to a queued message - in practice the booking receipt PDF.
 *
 * <p>Its own table, not a column on {@link Notification}. The delivery log is
 * listed and paginated constantly by the admin screen, and a receipt PDF is
 * tens of kilobytes; keeping the bytes out of that row means listing the log
 * never drags megabytes of PDF through the database for a page nobody is
 * looking at. Loaded lazily, and only the transport ever touches it.
 */
@Entity
@Table(name = "notification_attachment")
public class NotificationAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attachment_id")
    private Long attachmentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    @Column(name = "filename", nullable = false, length = 255)
    private String filename;

    @Column(name = "content_type", nullable = false, length = 120)
    private String contentType;

    /**
     * An explicit length, for the same reason the text columns carry one:
     * Hibernate 6 maps an unqualified {@code @Lob byte[]} onto MySQL's
     * <em>tinyblob</em> (255 bytes). 16MB gives mediumblob, comfortably above
     * the enqueue-time size cap.
     */
    @Lob
    @Column(name = "content", nullable = false, length = 16_777_215)
    private byte[] content;

    /** Stored rather than derived, so listing metadata never loads the bytes. */
    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    public NotificationAttachment() {
    }

    public NotificationAttachment(Notification notification, String filename, String contentType, byte[] content) {
        this.notification = notification;
        this.filename = filename;
        this.contentType = contentType;
        this.content = content;
        this.sizeBytes = content == null ? 0 : content.length;
    }

    public Long getAttachmentId() { return attachmentId; }
    public void setAttachmentId(Long attachmentId) { this.attachmentId = attachmentId; }

    public Notification getNotification() { return notification; }
    public void setNotification(Notification notification) { this.notification = notification; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public byte[] getContent() { return content; }
    public void setContent(byte[] content) {
        this.content = content;
        this.sizeBytes = content == null ? 0 : content.length;
    }

    public long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(long sizeBytes) { this.sizeBytes = sizeBytes; }
}
