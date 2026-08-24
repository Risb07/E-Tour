package com.etour.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * BRD "e-Mail Us" / marketing signup. Kept deliberately separate from User:
 * subscribing is public and must not create an account or imply one exists.
 */
@Entity
@Table(name = "newsletter_subscriber")
public class NewsletterSubscriber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscriber_id")
    private Long subscriberId;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    @Size(max = 190, message = "Email cannot exceed 190 characters")
    // 190 (not 255) so the unique index fits in MySQL's utf8mb4 key limit.
    @Column(nullable = false, unique = true, length = 190)
    private String email;

    @Size(max = 100)
    @Column(length = 100)
    private String name;

    /**
     * Soft unsubscribe. Re-subscribing an existing address flips this back to
     * true rather than inserting a duplicate row, which keeps the original
     * subscribedAt date and avoids a unique-constraint violation.
     */
    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "subscribed_at", nullable = false)
    private LocalDateTime subscribedAt = LocalDateTime.now();

    @Column(name = "unsubscribed_at")
    private LocalDateTime unsubscribedAt;

    public NewsletterSubscriber() {
    }

    public Long getSubscriberId() { return subscriberId; }
    public void setSubscriberId(Long subscriberId) { this.subscriberId = subscriberId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public LocalDateTime getSubscribedAt() { return subscribedAt; }
    public void setSubscribedAt(LocalDateTime subscribedAt) { this.subscribedAt = subscribedAt; }
    public LocalDateTime getUnsubscribedAt() { return unsubscribedAt; }
    public void setUnsubscribedAt(LocalDateTime unsubscribedAt) { this.unsubscribedAt = unsubscribedAt; }
}
