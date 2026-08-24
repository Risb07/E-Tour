package com.etour.dto;

import java.math.BigDecimal;

public class BookingAddonResponse {

    private Long bookingAddonId;
    private Long addonId;
    private String addonName;
    private String priceType;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal totalAddonCost;

    public BookingAddonResponse() {
    }

    public BookingAddonResponse(Long bookingAddonId, Long addonId, String addonName, String priceType,
            BigDecimal unitPrice, Integer quantity, BigDecimal totalAddonCost) {
        this.bookingAddonId = bookingAddonId;
        this.addonId = addonId;
        this.addonName = addonName;
        this.priceType = priceType;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.totalAddonCost = totalAddonCost;
    }

    public Long getBookingAddonId() {
        return bookingAddonId;
    }

    public void setBookingAddonId(Long bookingAddonId) {
        this.bookingAddonId = bookingAddonId;
    }

    public Long getAddonId() {
        return addonId;
    }

    public void setAddonId(Long addonId) {
        this.addonId = addonId;
    }

    public String getAddonName() {
        return addonName;
    }

    public void setAddonName(String addonName) {
        this.addonName = addonName;
    }

    public String getPriceType() {
        return priceType;
    }

    public void setPriceType(String priceType) {
        this.priceType = priceType;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getTotalAddonCost() {
        return totalAddonCost;
    }

    public void setTotalAddonCost(BigDecimal totalAddonCost) {
        this.totalAddonCost = totalAddonCost;
    }
}
