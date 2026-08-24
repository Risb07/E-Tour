package com.etour.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Everything the payment page shows before the customer commits: tour, dates,
 * passengers, room requirement and the money breakdown.
 *
 * The totals are computed server-side from the persisted booking, so the
 * amount the customer sees here is exactly the amount that will be charged -
 * the client never calculates it.
 */
public class PaymentSummaryResponse {

    private Long bookingId;
    private String bookingStatus;
    private String orderNumber;

    private Long tourId;
    private String tourTitle;
    private LocalDate departureDate;
    private LocalDate returnDate;
    private Integer numberOfPassengers;

    private List<PassengerDto> passengers;
    private RoomSummary roomSummary;
    private List<CostBreakdownLine> breakdown;
    private List<BookingAddonResponse> addons;

    private BigDecimal subTotal;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal gstRatePercent;
    private BigDecimal grandTotal;

    /** True once a successful payment exists - lets the UI block a double charge. */
    private boolean alreadyPaid;

    public PaymentSummaryResponse() {
    }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    public String getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(String bookingStatus) { this.bookingStatus = bookingStatus; }
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public Long getTourId() { return tourId; }
    public void setTourId(Long tourId) { this.tourId = tourId; }
    public String getTourTitle() { return tourTitle; }
    public void setTourTitle(String tourTitle) { this.tourTitle = tourTitle; }
    public LocalDate getDepartureDate() { return departureDate; }
    public void setDepartureDate(LocalDate departureDate) { this.departureDate = departureDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
    public Integer getNumberOfPassengers() { return numberOfPassengers; }
    public void setNumberOfPassengers(Integer numberOfPassengers) { this.numberOfPassengers = numberOfPassengers; }
    public List<PassengerDto> getPassengers() { return passengers; }
    public void setPassengers(List<PassengerDto> passengers) { this.passengers = passengers; }
    public RoomSummary getRoomSummary() { return roomSummary; }
    public void setRoomSummary(RoomSummary roomSummary) { this.roomSummary = roomSummary; }
    public List<CostBreakdownLine> getBreakdown() { return breakdown; }
    public void setBreakdown(List<CostBreakdownLine> breakdown) { this.breakdown = breakdown; }
    public List<BookingAddonResponse> getAddons() { return addons; }
    public void setAddons(List<BookingAddonResponse> addons) { this.addons = addons; }
    public BigDecimal getSubTotal() { return subTotal; }
    public void setSubTotal(BigDecimal subTotal) { this.subTotal = subTotal; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }
    public BigDecimal getGstRatePercent() { return gstRatePercent; }
    public void setGstRatePercent(BigDecimal gstRatePercent) { this.gstRatePercent = gstRatePercent; }
    public BigDecimal getGrandTotal() { return grandTotal; }
    public void setGrandTotal(BigDecimal grandTotal) { this.grandTotal = grandTotal; }
    public boolean isAlreadyPaid() { return alreadyPaid; }
    public void setAlreadyPaid(boolean alreadyPaid) { this.alreadyPaid = alreadyPaid; }
}
