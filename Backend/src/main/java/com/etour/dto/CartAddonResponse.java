package com.etour.dto;

import java.math.BigDecimal;

public class CartAddonResponse {
    private Long cartAddonId;
    private Long addonId;
    private String addonName;
    private Integer quantity;
    private BigDecimal estimatedCost;

    public CartAddonResponse() {}

    public CartAddonResponse(Long cartAddonId, Long addonId, String addonName, Integer quantity, BigDecimal estimatedCost) {
        this.cartAddonId = cartAddonId;
        this.addonId = addonId;
        this.addonName = addonName;
        this.quantity = quantity;
        this.estimatedCost = estimatedCost;
    }

    public Long getCartAddonId() { return cartAddonId; }
    public void setCartAddonId(Long cartAddonId) { this.cartAddonId = cartAddonId; }
    public Long getAddonId() { return addonId; }
    public void setAddonId(Long addonId) { this.addonId = addonId; }
    public String getAddonName() { return addonName; }
    public void setAddonName(String addonName) { this.addonName = addonName; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getEstimatedCost() { return estimatedCost; }
    public void setEstimatedCost(BigDecimal estimatedCost) { this.estimatedCost = estimatedCost; }
}
