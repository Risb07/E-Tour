package com.etour.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Pre-payment price preview (BRD 3.7 "Done" summary) - no booking is
 * persisted for this call. The same TourPricingCalculator is used here and
 * in BookingServiceImpl.createBooking, so the number shown here matches what
 * gets charged.
 */
public class BookingQuoteResponse {

    private Long scheduleId;
    private RoomSummary roomSummary;
    private List<CostBreakdownLine> breakdown;
    /**
     * Per-passenger name / type / occupancy / price, in submitted order.
     * Additive to `breakdown`, which stays the grouped view.
     */
    private List<PassengerPriceLine> passengerLines;
    private BigDecimal passengersTotal;

    public List<PassengerPriceLine> getPassengerLines() { return passengerLines; }
    public void setPassengerLines(List<PassengerPriceLine> passengerLines) { this.passengerLines = passengerLines; }
    private BigDecimal addonsTotal;
    private BigDecimal totalAmount;

    public BookingQuoteResponse() {
    }

    public BookingQuoteResponse(Long scheduleId, RoomSummary roomSummary, List<CostBreakdownLine> breakdown,
            BigDecimal passengersTotal, BigDecimal addonsTotal, BigDecimal totalAmount) {
        this.scheduleId = scheduleId;
        this.roomSummary = roomSummary;
        this.breakdown = breakdown;
        this.passengersTotal = passengersTotal;
        this.addonsTotal = addonsTotal;
        this.totalAmount = totalAmount;
    }

    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }
    public RoomSummary getRoomSummary() { return roomSummary; }
    public void setRoomSummary(RoomSummary roomSummary) { this.roomSummary = roomSummary; }
    public List<CostBreakdownLine> getBreakdown() { return breakdown; }
    public void setBreakdown(List<CostBreakdownLine> breakdown) { this.breakdown = breakdown; }
    public BigDecimal getPassengersTotal() { return passengersTotal; }
    public void setPassengersTotal(BigDecimal passengersTotal) { this.passengersTotal = passengersTotal; }
    public BigDecimal getAddonsTotal() { return addonsTotal; }
    public void setAddonsTotal(BigDecimal addonsTotal) { this.addonsTotal = addonsTotal; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
}
