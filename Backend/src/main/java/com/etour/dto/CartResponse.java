package com.etour.dto;

import java.math.BigDecimal;
import java.util.List;

public class CartResponse {

    private Long cartId;
    private Long scheduleId;
    private Long tourId;
    private String tourTitle;
    private String paxSummary;
    // Party composition behind paxSummary, so the cart can show "2 adults,
    // 1 child" without parsing the summary string. Null on older rows.
    private Integer adultCount;
    private Integer childCount;
    private BigDecimal estimatedAmount;
    private String status;
    private List<CartAddonResponse> addons;

    public Integer getAdultCount() { return adultCount; }
    public void setAdultCount(Integer adultCount) { this.adultCount = adultCount; }
    public Integer getChildCount() { return childCount; }
    public void setChildCount(Integer childCount) { this.childCount = childCount; }

    public CartResponse() {}

    public CartResponse(Long cartId, Long scheduleId, Long tourId, String tourTitle, String paxSummary,
            BigDecimal estimatedAmount, String status, List<CartAddonResponse> addons) {
        this.cartId = cartId;
        this.scheduleId = scheduleId;
        this.tourId = tourId;
        this.tourTitle = tourTitle;
        this.paxSummary = paxSummary;
        this.estimatedAmount = estimatedAmount;
        this.status = status;
        this.addons = addons;
    }

    public Long getCartId() { return cartId; }
    public void setCartId(Long cartId) { this.cartId = cartId; }
    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }
    public Long getTourId() { return tourId; }
    public void setTourId(Long tourId) { this.tourId = tourId; }
    public String getTourTitle() { return tourTitle; }
    public void setTourTitle(String tourTitle) { this.tourTitle = tourTitle; }
    public String getPaxSummary() { return paxSummary; }
    public void setPaxSummary(String paxSummary) { this.paxSummary = paxSummary; }
    public BigDecimal getEstimatedAmount() { return estimatedAmount; }
    public void setEstimatedAmount(BigDecimal estimatedAmount) { this.estimatedAmount = estimatedAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<CartAddonResponse> getAddons() { return addons; }
    public void setAddons(List<CartAddonResponse> addons) { this.addons = addons; }
}
