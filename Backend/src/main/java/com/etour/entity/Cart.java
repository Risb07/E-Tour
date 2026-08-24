package com.etour.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.etour.enums.CartStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "cart")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Long cartId;

    // Simplification vs. the full BRD flow: this backend requires login
    // before adding to cart, so customer is always set (no anonymous
    // session_token cart - see README "Known simplifications").
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnoreProperties({ "user", "hibernateLazyInitializer", "handler" })
    private Customer customer;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "schedule_id", nullable = false)
    @JsonIgnoreProperties({ "tour", "hibernateLazyInitializer", "handler" })
    private TourSchedule schedule;

    @Column(name = "pax_summary", length = 100)
    private String paxSummary;

    /**
     * Party composition chosen on the tour page. Stored as real numbers rather
     * than only inside the paxSummary string, so checkout can hand them to the
     * booking without re-parsing prose. Null on cart rows created before these
     * columns existed - checkout then falls back to the paxSummary count.
     */
    @Column(name = "adult_count")
    private Integer adultCount;

    @Column(name = "child_count")
    private Integer childCount;

    public Integer getAdultCount() { return adultCount; }
    public void setAdultCount(Integer adultCount) { this.adultCount = adultCount; }
    public Integer getChildCount() { return childCount; }
    public void setChildCount(Integer childCount) { this.childCount = childCount; }

    @Column(name = "estimated_amount", precision = 10, scale = 2)
    private BigDecimal estimatedAmount;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private CartStatus status = CartStatus.ACTIVE;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Long getCartId() { return cartId; }
    public void setCartId(Long cartId) { this.cartId = cartId; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public TourSchedule getSchedule() { return schedule; }
    public void setSchedule(TourSchedule schedule) { this.schedule = schedule; }
    public String getPaxSummary() { return paxSummary; }
    public void setPaxSummary(String paxSummary) { this.paxSummary = paxSummary; }
    public BigDecimal getEstimatedAmount() { return estimatedAmount; }
    public void setEstimatedAmount(BigDecimal estimatedAmount) { this.estimatedAmount = estimatedAmount; }
    public CartStatus getStatus() { return status; }
    public void setStatus(CartStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
