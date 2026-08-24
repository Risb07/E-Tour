package com.etour.enums;

import java.util.List;

/**
 * BRD 3.7 room options. Chosen per passenger, and now scoped by passenger
 * type: an adult can never be booked into a child category and vice versa
 * (see {@link #appliesTo(PassengerType)}).
 *
 * Maps onto the TourCost columns as:
 *   TWIN                -> basePrice             (2 sharing a room, the standard fare)
 *   SINGLE              -> singlePersonCost      (room to yourself, usually a supplement)
 *   TRIPLE              -> extraPersonCost       (3rd adult on an extra bed in a twin room)
 *   EXTRA_BED           -> extraPersonCost       (additional adult sharing, same rate)
 *   CHILD_WITH_BED      -> childWithBedCost      (child occupying their own bed)
 *   CHILD_WITHOUT_BED   -> childWithoutBedCost   (child sharing an adult's bed)
 *
 * TRIPLE is retained even though the booking form now offers only Twin /
 * Single / Extra person: existing passenger rows and admin room-charge rows
 * still reference it, and it prices identically to EXTRA_BED.
 *
 * Infants carry no occupancy at all - they are free and do not occupy a bed.
 */
public enum Occupancy {
    TWIN,
    SINGLE,
    TRIPLE,
    EXTRA_BED,
    CHILD_WITH_BED,
    CHILD_WITHOUT_BED;

    /** Adult room options, in the order the booking form presents them. */
    public static final List<Occupancy> ADULT_OPTIONS = List.of(TWIN, SINGLE, EXTRA_BED);

    /** Child options, in the order the booking form presents them. */
    public static final List<Occupancy> CHILD_OPTIONS = List.of(CHILD_WITH_BED, CHILD_WITHOUT_BED);

    public boolean isChildOption() {
        return this == CHILD_WITH_BED || this == CHILD_WITHOUT_BED;
    }

    public boolean isAdultOption() {
        return !isChildOption();
    }

    /**
     * Whether this option may be selected for the given passenger type. This
     * is the single rule behind the "adult options never appear for a child,
     * child options never appear for an adult" requirement, enforced
     * server-side so a hand-crafted request cannot slip an invalid pairing
     * past the UI.
     */
    public boolean appliesTo(PassengerType type) {
        if (type == null) {
            return true; // unspecified type - legacy request, nothing to contradict
        }
        return switch (type) {
            case ADULT -> isAdultOption();
            case CHILD -> isChildOption();
            case INFANT -> false; // infants are free and bedless
        };
    }

    /** The option a passenger of this type gets when none was chosen. */
    public static Occupancy defaultFor(PassengerType type, Boolean needsExtraBed) {
        if (type == null) {
            return TWIN;
        }
        return switch (type) {
            case ADULT -> TWIN;
            // Falls back to the legacy per-passenger flag so a request that
            // still sends needsExtraBed instead of a child category prices
            // exactly as it did before.
            case CHILD -> (needsExtraBed == null || needsExtraBed) ? CHILD_WITH_BED : CHILD_WITHOUT_BED;
            case INFANT -> null;
        };
    }

    public String getLabel() {
        return switch (this) {
            case TWIN -> "Twin sharing";
            case SINGLE -> "Single occupancy";
            case TRIPLE -> "Triple occupancy";
            case EXTRA_BED -> "Extra person";
            case CHILD_WITH_BED -> "Child with bed";
            case CHILD_WITHOUT_BED -> "Child without bed";
        };
    }
}
