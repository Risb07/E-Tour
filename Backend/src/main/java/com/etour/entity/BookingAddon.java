package com.etour.entity;

import java.math.BigDecimal;

import com.etour.enums.PriceType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;

@Entity
@Table(name = "booking_addon")
public class BookingAddon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_addon_id")
    private Long bookingAddonId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    @JsonIgnoreProperties({ "customer", "schedule", "hibernateLazyInitializer", "handler" })
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "addon_id", nullable = false)
    @JsonIgnoreProperties({ "tour", "hibernateLazyInitializer", "handler" })
    private TourAddon addon;

    // Snapshot fields: the catalogue TourAddon can change price later,
    // this row must not — the invoice is frozen at booking time.
    @Column(name = "addon_name", nullable = false, length = 150)
    private String addonName;

    @Enumerated(EnumType.STRING)
    @Column(name = "price_type", nullable = false, length = 20)
    private PriceType priceType;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private Integer quantity = 1;

    @Column(name = "total_addon_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAddonCost;

    public Long getBookingAddonId() { return bookingAddonId; }
    public void setBookingAddonId(Long bookingAddonId) { this.bookingAddonId = bookingAddonId; }
    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }
    public TourAddon getAddon() { return addon; }
    public void setAddon(TourAddon addon) { this.addon = addon; }
    public String getAddonName() { return addonName; }
    public void setAddonName(String addonName) { this.addonName = addonName; }
    public PriceType getPriceType() { return priceType; }
    public void setPriceType(PriceType priceType) { this.priceType = priceType; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getTotalAddonCost() { return totalAddonCost; }
    public void setTotalAddonCost(BigDecimal totalAddonCost) { this.totalAddonCost = totalAddonCost; }
}
