package com.etour.dto;

import java.math.BigDecimal;

import com.etour.enums.Occupancy;
import com.etour.enums.PassengerType;

/**
 * What one named passenger costs, and why.
 *
 * The grouped {@link CostBreakdownLine} list answers "how much for the four
 * adults" - useful for an invoice, useless for a customer checking that their
 * daughter was booked without a bed. This line answers the per-person
 * question the booking summary asks: name, type, occupancy category, price.
 *
 * The type and occupancy here are the RESOLVED values the server actually
 * priced on, not the raw request values, so a legacy request that sent
 * neither still produces a fully-populated line.
 */
public class PassengerPriceLine {

    /** Position in the submitted passenger list, so a client can match rows up. */
    private Integer passengerIndex;
    private String fullName;

    private String passengerType;
    private String passengerTypeLabel;

    /** Null for infants, who occupy no bed. */
    private String occupancy;
    private String occupancyLabel;

    /** The TourCost category rate before any room supplement. */
    private BigDecimal categoryRate;

    /** Admin-configured supplement for the chosen occupancy, usually zero. */
    private BigDecimal roomCharge;

    /** categoryRate + roomCharge - what this passenger contributes to the total. */
    private BigDecimal price;

    public PassengerPriceLine() {
    }

    public PassengerPriceLine(Integer passengerIndex, String fullName, PassengerType passengerType,
            Occupancy occupancy, BigDecimal categoryRate, BigDecimal roomCharge, BigDecimal price) {
        this.passengerIndex = passengerIndex;
        this.fullName = fullName;
        this.passengerType = passengerType == null ? null : passengerType.name();
        this.passengerTypeLabel = passengerType == null ? null : passengerType.getLabel();
        this.occupancy = occupancy == null ? null : occupancy.name();
        this.occupancyLabel = occupancy == null ? null : occupancy.getLabel();
        this.categoryRate = categoryRate;
        this.roomCharge = roomCharge;
        this.price = price;
    }

    public Integer getPassengerIndex() { return passengerIndex; }
    public void setPassengerIndex(Integer passengerIndex) { this.passengerIndex = passengerIndex; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPassengerType() { return passengerType; }
    public void setPassengerType(String passengerType) { this.passengerType = passengerType; }
    public String getPassengerTypeLabel() { return passengerTypeLabel; }
    public void setPassengerTypeLabel(String passengerTypeLabel) { this.passengerTypeLabel = passengerTypeLabel; }
    public String getOccupancy() { return occupancy; }
    public void setOccupancy(String occupancy) { this.occupancy = occupancy; }
    public String getOccupancyLabel() { return occupancyLabel; }
    public void setOccupancyLabel(String occupancyLabel) { this.occupancyLabel = occupancyLabel; }
    public BigDecimal getCategoryRate() { return categoryRate; }
    public void setCategoryRate(BigDecimal categoryRate) { this.categoryRate = categoryRate; }
    public BigDecimal getRoomCharge() { return roomCharge; }
    public void setRoomCharge(BigDecimal roomCharge) { this.roomCharge = roomCharge; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}
