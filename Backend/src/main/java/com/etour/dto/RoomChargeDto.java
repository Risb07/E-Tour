package com.etour.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Room-sharing charge for a tour. Used for both admin writes and public reads. */
public class RoomChargeDto {

    private Long roomChargeId;
    private Long tourId;

    @NotNull(message = "Occupancy is required")
    @Pattern(regexp = "TWIN|SINGLE|TRIPLE|EXTRA_BED",
            message = "Occupancy must be TWIN, SINGLE, TRIPLE or EXTRA_BED")
    private String occupancy;

    @NotNull(message = "Charge is required")
    @DecimalMin(value = "0.0", message = "Charge cannot be negative")
    private BigDecimal charge;

    @Size(max = 200, message = "Description cannot exceed 200 characters")
    private String description;

    private Boolean active = true;

    public RoomChargeDto() {
    }

    public RoomChargeDto(Long roomChargeId, Long tourId, String occupancy, BigDecimal charge,
            String description, Boolean active) {
        this.roomChargeId = roomChargeId;
        this.tourId = tourId;
        this.occupancy = occupancy;
        this.charge = charge;
        this.description = description;
        this.active = active;
    }

    public Long getRoomChargeId() { return roomChargeId; }
    public void setRoomChargeId(Long roomChargeId) { this.roomChargeId = roomChargeId; }
    public Long getTourId() { return tourId; }
    public void setTourId(Long tourId) { this.tourId = tourId; }
    public String getOccupancy() { return occupancy; }
    public void setOccupancy(String occupancy) { this.occupancy = occupancy; }
    public BigDecimal getCharge() { return charge; }
    public void setCharge(BigDecimal charge) { this.charge = charge; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
