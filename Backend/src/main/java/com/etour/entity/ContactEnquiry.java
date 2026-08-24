package com.etour.entity;

import java.time.LocalDateTime;

import com.etour.enums.EnquiryStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * An enquiry submitted from the public Contact page.
 *
 * Deliberately standalone, like NewsletterSubscriber: anyone can write in
 * without an account, so this must never be linked to User or Customer or
 * imply that a matching account exists. The sender's details are captured as
 * they typed them rather than resolved against a profile.
 *
 * Persisted rather than only e-mailed, so an enquiry survives a mail outage
 * and the admin module has something to list, search, filter and close.
 */
@Entity
@Table(name = "contact_enquiry")
public class ContactEnquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enquiry_id")
    private Long enquiryId;

    @NotBlank(message = "Name is required")
    @Size(max = 150, message = "Name cannot exceed 150 characters")
    @Column(nullable = false, length = 150)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    @Size(max = 190, message = "Email cannot exceed 190 characters")
    // 190 rather than 255 for consistency with newsletter_subscriber, so the
    // column can carry an index inside MySQL's utf8mb4 key limit if searching
    // by sender ever needs one.
    @Column(nullable = false, length = 190)
    private String email;

    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    @Column(length = 20)
    private String phone;

    @NotBlank(message = "Subject is required")
    @Size(max = 200, message = "Subject cannot exceed 200 characters")
    @Column(nullable = false, length = 200)
    private String subject;

    @NotBlank(message = "Message is required")
    @Size(max = 4000, message = "Message cannot exceed 4000 characters")
    @Column(nullable = false, length = 4000)
    private String message;

    /**
     * Always NEW on arrival - the status is never taken from the request, so a
     * sender cannot submit something pre-marked resolved.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EnquiryStatus status = EnquiryStatus.NEW;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /** Set when an admin last moved the enquiry along. Null while untouched. */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public ContactEnquiry() {
    }

    public Long getEnquiryId() { return enquiryId; }
    public void setEnquiryId(Long enquiryId) { this.enquiryId = enquiryId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public EnquiryStatus getStatus() { return status; }
    public void setStatus(EnquiryStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
