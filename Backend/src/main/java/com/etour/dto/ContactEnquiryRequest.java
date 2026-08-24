package com.etour.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Public contact-form payload.
 *
 * Note what is absent: no status and no id. The lifecycle state is set by the
 * server (always NEW), so a sender cannot submit an enquiry that arrives
 * pre-resolved and is never read.
 */
public class ContactEnquiryRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 150, message = "Name cannot exceed 150 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    @Size(max = 190, message = "Email cannot exceed 190 characters")
    private String email;

    // Optional, but must look like a phone number when supplied. Matches the
    // 10-digit rule RegisterRequest already uses, with an empty string allowed
    // so an untouched field doesn't fail validation.
    @Pattern(regexp = "^$|^[0-9]{10}$", message = "Phone number must contain exactly 10 digits")
    private String phone;

    @NotBlank(message = "Subject is required")
    @Size(max = 200, message = "Subject cannot exceed 200 characters")
    private String subject;

    @NotBlank(message = "Message is required")
    @Size(max = 4000, message = "Message cannot exceed 4000 characters")
    private String message;

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
}
