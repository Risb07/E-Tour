package com.etour.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * A named subject/body pair with {{placeholder}} slots.
 *
 * <p>Templates live in this service's own database rather than in a properties
 * file so they are listable through the API and editable without a redeploy -
 * which is the point of pulling notifications out into a service at all.
 */
@Entity
@Table(name = "notification_template",
        uniqueConstraints = @UniqueConstraint(name = "uk_template_code", columnNames = "code"))
public class NotificationTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_id")
    private Long templateId;

    @Column(name = "code", nullable = false, length = 64)
    private String code;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "subject", nullable = false, length = 500)
    private String subject;

    // NOT @Lob - see the comment on Notification.body. A @Lob String becomes
    // tinytext (255 bytes) on MySQL, which is shorter than every template here.
    @Column(name = "body", nullable = false, length = 65535)
    private String body;

    public NotificationTemplate() {
    }

    public NotificationTemplate(String code, String description, String subject, String body) {
        this.code = code;
        this.description = description;
        this.subject = subject;
        this.body = body;
    }

    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
}
