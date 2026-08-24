package com.etour.entity;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "cart_addon")
public class CartAddon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_addon_id")
    private Long cartAddonId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    @JsonIgnoreProperties({ "customer", "schedule", "hibernateLazyInitializer", "handler" })
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "addon_id", nullable = false)
    @JsonIgnoreProperties({ "tour", "hibernateLazyInitializer", "handler" })
    private TourAddon addon;

    @Column(nullable = false)
    private Integer quantity = 1;

    @Column(name = "estimated_cost", precision = 10, scale = 2)
    private BigDecimal estimatedCost;

    public Long getCartAddonId() { return cartAddonId; }
    public void setCartAddonId(Long cartAddonId) { this.cartAddonId = cartAddonId; }
    public Cart getCart() { return cart; }
    public void setCart(Cart cart) { this.cart = cart; }
    public TourAddon getAddon() { return addon; }
    public void setAddon(TourAddon addon) { this.addon = addon; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getEstimatedCost() { return estimatedCost; }
    public void setEstimatedCost(BigDecimal estimatedCost) { this.estimatedCost = estimatedCost; }
}
