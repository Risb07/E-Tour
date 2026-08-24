package com.etour.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Flattened wishlist row - carries just enough tour detail to render a card
 * without the frontend needing a follow-up request per tour.
 */
public class WishlistItemResponse {

    private Long wishlistItemId;
    private Long tourId;
    private String tourTitle;
    private String tourCode;
    private Integer durationDays;
    private BigDecimal basePrice;
    private LocalDateTime createdAt;

    public WishlistItemResponse() {
    }

    public WishlistItemResponse(Long wishlistItemId, Long tourId, String tourTitle, String tourCode,
            Integer durationDays, BigDecimal basePrice, LocalDateTime createdAt) {
        this.wishlistItemId = wishlistItemId;
        this.tourId = tourId;
        this.tourTitle = tourTitle;
        this.tourCode = tourCode;
        this.durationDays = durationDays;
        this.basePrice = basePrice;
        this.createdAt = createdAt;
    }

    public Long getWishlistItemId() { return wishlistItemId; }
    public void setWishlistItemId(Long wishlistItemId) { this.wishlistItemId = wishlistItemId; }
    public Long getTourId() { return tourId; }
    public void setTourId(Long tourId) { this.tourId = tourId; }
    public String getTourTitle() { return tourTitle; }
    public void setTourTitle(String tourTitle) { this.tourTitle = tourTitle; }
    public String getTourCode() { return tourCode; }
    public void setTourCode(String tourCode) { this.tourCode = tourCode; }
    public Integer getDurationDays() { return durationDays; }
    public void setDurationDays(Integer durationDays) { this.durationDays = durationDays; }
    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
