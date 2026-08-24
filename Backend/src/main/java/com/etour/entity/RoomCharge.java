package com.etour.entity;

import java.math.BigDecimal;

import com.etour.enums.Occupancy;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Per-tour charge for a room-sharing option (BRD 3.7 room options).
 *
 * Previously the occupancy a traveller chose was priced from the generic
 * TourCost columns and the choice itself was never stored. Admins had no way
 * to say "a single room on THIS tour costs 4,500 extra". This table makes the
 * charge explicit, per tour, per occupancy type, and editable from the admin
 * portal.
 *
 * The amount is a SUPPLEMENT added on top of the per-person fare, not a
 * replacement for it - so twin sharing is normally 0 and single occupancy
 * carries the supplement. A tour with no rows here falls back to the old
 * TourCost behaviour, so existing tours keep working unchanged.
 */
@Entity
@Table(name = "room_charge", uniqueConstraints = @UniqueConstraint(
        name = "uk_room_charge_tour_occupancy", columnNames = { "tour_id", "occupancy" }))
public class RoomCharge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_charge_id")
    private Long roomChargeId;

    @NotNull(message = "Tour is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tour_id", nullable = false)
    @JsonIgnoreProperties({ "categories", "tourCosts", "hibernateLazyInitializer", "handler" })
    private Tour tour;

    @NotNull(message = "Occupancy type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "occupancy", nullable = false, length = 20)
    private Occupancy occupancy;

    /**
     * Supplement per person for this occupancy, added to the base fare.
     * Zero is valid and meaningful (twin sharing = no supplement).
     */
    @NotNull(message = "Charge is required")
    @DecimalMin(value = "0.0", message = "Charge cannot be negative")
    @Column(name = "charge", nullable = false, precision = 10, scale = 2)
    private BigDecimal charge = BigDecimal.ZERO;

    /** Shown to the customer next to the option, e.g. "Private room". */
    @Size(max = 200)
    @Column(name = "description", length = 200)
    private String description;

    /** Retire an option without deleting it (and losing past bookings' context). */
    @Column(nullable = false)
    private Boolean active = true;

    public RoomCharge() {
    }

    public Long getRoomChargeId() { return roomChargeId; }
    public void setRoomChargeId(Long roomChargeId) { this.roomChargeId = roomChargeId; }
    public Tour getTour() { return tour; }
    public void setTour(Tour tour) { this.tour = tour; }
    public Occupancy getOccupancy() { return occupancy; }
    public void setOccupancy(Occupancy occupancy) { this.occupancy = occupancy; }
    public BigDecimal getCharge() { return charge; }
    public void setCharge(BigDecimal charge) { this.charge = charge; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
