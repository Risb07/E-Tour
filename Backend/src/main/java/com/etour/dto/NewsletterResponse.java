package com.etour.dto;

import java.time.LocalDateTime;

/** Admin-facing view of a subscriber. */
public class NewsletterResponse {

    private Long subscriberId;
    private String email;
    private String name;
    private Boolean active;
    private LocalDateTime subscribedAt;
    private LocalDateTime unsubscribedAt;

    public NewsletterResponse() {
    }

    public NewsletterResponse(Long subscriberId, String email, String name, Boolean active,
            LocalDateTime subscribedAt, LocalDateTime unsubscribedAt) {
        this.subscriberId = subscriberId;
        this.email = email;
        this.name = name;
        this.active = active;
        this.subscribedAt = subscribedAt;
        this.unsubscribedAt = unsubscribedAt;
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
