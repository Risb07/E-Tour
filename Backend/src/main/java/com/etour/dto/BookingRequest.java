package com.etour.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class BookingRequest {

    @NotNull(message = "scheduleId is required")
    private Long scheduleId;

    @NotNull(message = "numberOfPassengers is required")
    @Min(value = 1, message = "numberOfPassengers must be at least 1")
    private Integer numberOfPassengers;

    /**
     * Party composition chosen on the tour page, before any passenger details
     * are collected. adultCount + childCount must equal numberOfPassengers,
     * and a booking must contain at least one adult - a party of children
     * alone has no lead traveller and, because infants are free, could
     * otherwise reach a zero total.
     *
     * Optional so that a request which predates these fields still works; the
     * at-least-one-adult rule is then enforced from the passengers' dates of
     * birth instead. See BookingServiceImpl#validateComposition.
     */
    @Min(value = 0, message = "adultCount cannot be negative")
    private Integer adultCount;

    @Min(value = 0, message = "childCount cannot be negative")
    private Integer childCount;

    public Integer getAdultCount() { return adultCount; }
    public void setAdultCount(Integer adultCount) { this.adultCount = adultCount; }
    public Integer getChildCount() { return childCount; }
    public void setChildCount(Integer childCount) { this.childCount = childCount; }

    // Optional add-ons selected at booking time (tour_addon IDs + quantity)
    private java.util.List<BookingAddonSelection> addons = new java.util.ArrayList<>();

    // Optional passenger details (BRD 3.7). When supplied, size must match
    // numberOfPassengers and the booking is priced immediately using
    // age-banded TourCost rates (see TourPricingCalculator) with the
    // passengers persisted in the same transaction. When omitted (e.g. a
    // cart checkout, which only knows a headcount), the booking is created
    // with an ESTIMATED flat total and must go through
    // PATCH /api/bookings/{id}/passengers to finalize pricing before payment.
    private java.util.List<PassengerInput> passengers = new java.util.ArrayList<>();

    public java.util.List<PassengerInput> getPassengers() {
        return passengers;
    }

    public void setPassengers(java.util.List<PassengerInput> passengers) {
        this.passengers = passengers;
    }

    public Long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
    }

    public Integer getNumberOfPassengers() {
        return numberOfPassengers;
    }

    public void setNumberOfPassengers(Integer numberOfPassengers) {
        this.numberOfPassengers = numberOfPassengers;
    }

    public java.util.List<BookingAddonSelection> getAddons() {
        return addons;
    }

    public void setAddons(java.util.List<BookingAddonSelection> addons) {
        this.addons = addons;
    }

    public static class BookingAddonSelection {
        @NotNull
        private Long addonId;
        @NotNull
        @Min(1)
        private Integer quantity = 1;

        public Long getAddonId() {
            return addonId;
        }

        public void setAddonId(Long addonId) {
            this.addonId = addonId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }
}
