package com.etour.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

public class PassengerDto {

    private Long passengerId;

    @NotNull(message = "bookingId is required")
    private Long bookingId;

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String gender;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dob;

    private String nationality;
    private String idProofType;

    @NotBlank(message = "ID proof number is required")
    private String idProofNumber;

    private Boolean needsExtraBed = true;

    /** ADULT, CHILD or INFANT - the band this passenger was booked under. */
    private String passengerType;

    /** Occupancy category chosen, and the supplement charged for it at booking time. */
    private String occupancy;
    private java.math.BigDecimal roomCharge;

    /** What this passenger alone was priced at (category rate + supplement). */
    private java.math.BigDecimal passengerPrice;

    public String getPassengerType() { return passengerType; }
    public void setPassengerType(String passengerType) { this.passengerType = passengerType; }
    public String getOccupancy() { return occupancy; }
    public void setOccupancy(String occupancy) { this.occupancy = occupancy; }
    public java.math.BigDecimal getRoomCharge() { return roomCharge; }
    public void setRoomCharge(java.math.BigDecimal roomCharge) { this.roomCharge = roomCharge; }
    public java.math.BigDecimal getPassengerPrice() { return passengerPrice; }
    public void setPassengerPrice(java.math.BigDecimal passengerPrice) { this.passengerPrice = passengerPrice; }

    // BRD 3.7 primary-passenger address.
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String country;
    private String pincode;

    public String getAddressLine1() { return addressLine1; }
    public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }
    public String getAddressLine2() { return addressLine2; }
    public void setAddressLine2(String addressLine2) { this.addressLine2 = addressLine2; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public Long getPassengerId() { return passengerId; }
    public void setPassengerId(Long passengerId) { this.passengerId = passengerId; }
    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }
    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }
    public String getIdProofType() { return idProofType; }
    public void setIdProofType(String idProofType) { this.idProofType = idProofType; }
    public String getIdProofNumber() { return idProofNumber; }
    public void setIdProofNumber(String idProofNumber) { this.idProofNumber = idProofNumber; }
    public Boolean getNeedsExtraBed() { return needsExtraBed; }
    public void setNeedsExtraBed(Boolean needsExtraBed) { this.needsExtraBed = needsExtraBed; }
}
