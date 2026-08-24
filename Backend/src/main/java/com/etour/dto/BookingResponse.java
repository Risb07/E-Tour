package com.etour.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class BookingResponse {

    private Long bookingId;
    private Long customerId;
    private String customerName;
    private Long scheduleId;
    private Long tourId;
    private String tourTitle;
    private LocalDate departureDate;
    private LocalDate bookingDate;
    private BigDecimal totalAmount;
    private String orderNumber;
    private String bookingStatus;
    private Integer numberOfPassengers;
    // Party composition, so the passenger step can render the right number of
    // adult and child forms without asking again. Null on older bookings.
    private Integer adultCount;
    private Integer childCount;
    private List<BookingAddonResponse> addons;
    private RoomSummary roomSummary;
    private List<CostBreakdownLine> breakdown;
    // Per-passenger name / type / occupancy / price, in submitted order.
    // Populated on the calls that price a booking (create, finalize);
    // null on plain reads, exactly like breakdown and roomSummary.
    private List<PassengerPriceLine> passengerLines;
    // true once Passenger rows exist for this booking (equal to
    // numberOfPassengers) and pricing has been finalized - i.e. it's safe to
    // pay. Cart-originated bookings start out false.
    private boolean passengersFinalized;

    public BookingResponse() {
    }

    public RoomSummary getRoomSummary() { return roomSummary; }
    public void setRoomSummary(RoomSummary roomSummary) { this.roomSummary = roomSummary; }
    public List<CostBreakdownLine> getBreakdown() { return breakdown; }
    public void setBreakdown(List<CostBreakdownLine> breakdown) { this.breakdown = breakdown; }
    public List<PassengerPriceLine> getPassengerLines() { return passengerLines; }
    public void setPassengerLines(List<PassengerPriceLine> passengerLines) { this.passengerLines = passengerLines; }
    public boolean isPassengersFinalized() { return passengersFinalized; }
    public void setPassengersFinalized(boolean passengersFinalized) { this.passengersFinalized = passengersFinalized; }

    public BookingResponse(Long bookingId, Long customerId, String customerName, Long scheduleId, Long tourId,
            String tourTitle, LocalDate departureDate, LocalDate bookingDate, BigDecimal totalAmount,
            String orderNumber, String bookingStatus, List<BookingAddonResponse> addons) {
        this.bookingId = bookingId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.scheduleId = scheduleId;
        this.tourId = tourId;
        this.tourTitle = tourTitle;
        this.departureDate = departureDate;
        this.bookingDate = bookingDate;
        this.totalAmount = totalAmount;
        this.orderNumber = orderNumber;
        this.bookingStatus = bookingStatus;
        this.addons = addons;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
    }

    public Long getTourId() {
        return tourId;
    }

    public void setTourId(Long tourId) {
        this.tourId = tourId;
    }

    public String getTourTitle() {
        return tourTitle;
    }

    public void setTourTitle(String tourTitle) {
        this.tourTitle = tourTitle;
    }

    public LocalDate getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(LocalDate departureDate) {
        this.departureDate = departureDate;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public List<BookingAddonResponse> getAddons() {
        return addons;
    }

    public void setAddons(List<BookingAddonResponse> addons) {
        this.addons = addons;
    }

    public Integer getNumberOfPassengers() {
        return numberOfPassengers;
    }

    public void setNumberOfPassengers(Integer numberOfPassengers) {
        this.numberOfPassengers = numberOfPassengers;
    }

    public Integer getAdultCount() {
        return adultCount;
    }

    public void setAdultCount(Integer adultCount) {
        this.adultCount = adultCount;
    }

    public Integer getChildCount() {
        return childCount;
    }

    public void setChildCount(Integer childCount) {
        this.childCount = childCount;
    }
}
