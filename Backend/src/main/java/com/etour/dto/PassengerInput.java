package com.etour.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Passenger details captured during booking (BRD 3.7 - Book Tour). Used both
 * to price a booking (age as of departure date drives Adult/Child/Infant
 * banding - see TourPricingCalculator) and, when the booking is actually
 * created, to persist the Passenger rows in the same transaction.
 */
public class PassengerInput {

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String gender;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dob;

    private String nationality;
    private String idProofType;

    @NotBlank(message = "ID proof number is required")
    private String idProofNumber;

    // Only meaningful for CHILD-banded passengers (age 2-12 as of departure).
    // true (default) = an extra bed is needed in the room -> childWithBedCost.
    // false = child shares an existing bed with an adult -> childWithoutBedCost.
    private Boolean needsExtraBed = true;

    /**
     * How this passenger is booked: ADULT, CHILD or INFANT.
     *
     * DERIVED, NOT ACCEPTED. The band comes from the date of birth above,
     * measured at the departure date (see TourPricingCalculator). This field
     * is kept on the DTO only so a caller reading a passenger back sees the
     * band that was applied - anything sent in is ignored, which is what stops
     * a request talking the server into an adult fare for a six-year-old.
     */
    private com.etour.enums.PassengerType passengerType;

    public com.etour.enums.PassengerType getPassengerType() { return passengerType; }
    public void setPassengerType(com.etour.enums.PassengerType passengerType) { this.passengerType = passengerType; }

    /**
     * BRD 3.7 occupancy category for this passenger. It must match the band
     * the date of birth implies: adults take TWIN, SINGLE or EXTRA_BED,
     * children take CHILD_WITH_BED or CHILD_WITHOUT_BED, and infants take
     * none. A category that contradicts the date of birth is rejected
     * (see Occupancy#appliesTo).
     *
     * When null the server falls back to the previous behaviour (pair adults
     * into twin rooms, odd one out pays the single supplement; children priced
     * by the needsExtraBed flag), so older clients keep working.
     */
    private com.etour.enums.Occupancy occupancy;

    public com.etour.enums.Occupancy getOccupancy() { return occupancy; }
    public void setOccupancy(com.etour.enums.Occupancy occupancy) { this.occupancy = occupancy; }

    // BRD 3.7 primary-passenger address. Optional per-passenger so a family
    // doesn't have to retype one address per traveller.
    @Size(max = 200, message = "Address line 1 cannot exceed 200 characters")
    private String addressLine1;

    @Size(max = 200, message = "Address line 2 cannot exceed 200 characters")
    private String addressLine2;

    @Size(max = 100, message = "City cannot exceed 100 characters")
    private String city;

    @Size(max = 100, message = "State cannot exceed 100 characters")
    private String state;

    @Size(max = 100, message = "Country cannot exceed 100 characters")
    private String country;

    @Pattern(regexp = "^$|^[A-Za-z0-9 -]{3,20}$", message = "Enter a valid pincode")
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
