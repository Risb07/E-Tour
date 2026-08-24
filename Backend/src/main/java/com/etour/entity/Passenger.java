package com.etour.entity;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

@Entity
@Table(name = "passenger")
public class Passenger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "passenger_id")
    private Long passengerId;

    @NotNull(message = "Booking is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    @JsonIgnoreProperties({ "customer", "schedule", "hibernateLazyInitializer", "handler" })
    private Booking booking;

    @NotBlank(message = "Full Name is required")
    @Column(name = "full_name", length = 150)
    private String fullName;

    @Column(name = "gender", length = 1)
    private String gender;

    @Past(message = "Date of birth must be in the past")
    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "nationality", length = 2)
    private String nationality;

    @Column(name = "id_proof_type")
    private String idProofType;

    @NotBlank(message = "ID Proof Number is required")
    @Column(name = "id_proof_number", unique = true, length = 50)
    private String idProofNumber;

    // Only meaningful when this passenger is priced in the CHILD band (age
    // 2-12 as of departure) - see TourPricingCalculator. Null/true = extra
    // bed needed (childWithBedCost); false = shares an adult's bed
    // (childWithoutBedCost).
    @Column(name = "needs_extra_bed")
    private Boolean needsExtraBed = true;

    /**
     * Whether this passenger was booked as an adult, a child or an infant.
     * Stored because it is now an explicit choice on the booking form rather
     * than something re-derivable from dob - two passengers with the same
     * birth date can legitimately be booked differently.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "passenger_type", length = 20)
    private com.etour.enums.PassengerType passengerType;

    /**
     * Occupancy category chosen for this passenger (BRD 3.7). Previously the
     * choice drove pricing but was never persisted, so a booking couldn't be
     * re-priced or audited afterwards. Adults hold TWIN / SINGLE / EXTRA_BED
     * (or legacy TRIPLE), children hold CHILD_WITH_BED / CHILD_WITHOUT_BED,
     * and infants hold null.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "occupancy", length = 20)
    private com.etour.enums.Occupancy occupancy;

    /**
     * What this individual passenger was priced at - the occupancy category
     * rate plus any room supplement. Captured at booking time alongside
     * roomCharge so the per-passenger figures shown on the summary, the
     * invoice and the admin view all agree, and stay agreeing after a later
     * admin price change.
     */
    @Column(name = "passenger_price", precision = 12, scale = 2)
    private java.math.BigDecimal passengerPrice;

    /**
     * The room supplement actually charged for this passenger, captured at
     * booking time. Stored rather than recomputed so a later admin price
     * change never rewrites what an existing customer was charged.
     */
    @Column(name = "room_charge", precision = 10, scale = 2)
    private java.math.BigDecimal roomCharge;

    // BRD 3.7 - primary passenger address. Optional at the entity level:
    // only the lead passenger's address is required, and that rule is
    // enforced at the request boundary rather than here, so co-travellers
    // don't each have to repeat the same address.
    @Column(name = "address_line1", length = 200)
    private String addressLine1;

    @Column(name = "address_line2", length = 200)
    private String addressLine2;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(length = 100)
    private String country;

    @Column(length = 20)
    private String pincode;

    public Long getPassengerId() { return passengerId; }
    public void setPassengerId(Long passengerId) { this.passengerId = passengerId; }
    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }
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
    public com.etour.enums.PassengerType getPassengerType() { return passengerType; }
    public void setPassengerType(com.etour.enums.PassengerType passengerType) { this.passengerType = passengerType; }
    public com.etour.enums.Occupancy getOccupancy() { return occupancy; }
    public void setOccupancy(com.etour.enums.Occupancy occupancy) { this.occupancy = occupancy; }
    public java.math.BigDecimal getRoomCharge() { return roomCharge; }
    public void setRoomCharge(java.math.BigDecimal roomCharge) { this.roomCharge = roomCharge; }
    public java.math.BigDecimal getPassengerPrice() { return passengerPrice; }
    public void setPassengerPrice(java.math.BigDecimal passengerPrice) { this.passengerPrice = passengerPrice; }
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
}
