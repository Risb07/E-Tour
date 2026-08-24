package com.etour.dto;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class CartRequest {

    @NotNull(message = "scheduleId is required")
    private Long scheduleId;

    @NotNull(message = "numberOfPassengers is required")
    @Min(1)
    private Integer numberOfPassengers;

    /**
     * Party composition, carried through checkout onto the booking so the
     * passenger step renders the right mix of forms. Optional for callers
     * that predate these fields.
     */
    @Min(value = 0, message = "adultCount cannot be negative")
    private Integer adultCount;

    @Min(value = 0, message = "childCount cannot be negative")
    private Integer childCount;

    private List<AddonSelection> addons = new ArrayList<>();

    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }
    public Integer getNumberOfPassengers() { return numberOfPassengers; }
    public void setNumberOfPassengers(Integer numberOfPassengers) { this.numberOfPassengers = numberOfPassengers; }
    public Integer getAdultCount() { return adultCount; }
    public void setAdultCount(Integer adultCount) { this.adultCount = adultCount; }
    public Integer getChildCount() { return childCount; }
    public void setChildCount(Integer childCount) { this.childCount = childCount; }
    public List<AddonSelection> getAddons() { return addons; }
    public void setAddons(List<AddonSelection> addons) { this.addons = addons; }

    public static class AddonSelection {
        @NotNull private Long addonId;
        @NotNull @Min(1) private Integer quantity = 1;

        public Long getAddonId() { return addonId; }
        public void setAddonId(Long addonId) { this.addonId = addonId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }
}
