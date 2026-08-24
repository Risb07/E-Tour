package com.etour.enums;

import java.time.LocalDate;
import java.time.Period;

/**
 * How a passenger is booked and priced.
 *
 * Previously this was never stored or chosen - the band was inferred purely
 * from date of birth at pricing time. The booking form now asks for it
 * explicitly (a child travelling on an adult's passport, a 12-year-old the
 * operator has agreed to seat as an adult, etc.), so the selection is the
 * source of truth and DOB only provides the default and a sanity check.
 *
 * The age boundaries below stay identical to the ones TourPricingCalculator
 * has always used, so a request that omits passengerType still bands exactly
 * as it did before.
 */
public enum PassengerType {

    /** Age 12+ as of departure. Priced by the chosen adult occupancy. */
    ADULT,

    /** Age 2-12 as of departure. Priced by child-with-bed / child-without-bed. */
    CHILD,

    /** Under 2 as of departure. Free, and never occupies a room of its own. */
    INFANT;

    private static final int INFANT_MAX_AGE = 2;
    private static final int CHILD_MAX_AGE = 12;

    /**
     * The band a date of birth implies. Used as the default when the client
     * did not send an explicit type, which is what keeps older clients (and
     * the cart checkout path) pricing exactly as before.
     *
     * A null dob is treated as an adult, matching the long-standing
     * calculator behaviour for incomplete legacy data.
     */
    public static PassengerType fromAge(LocalDate dob, LocalDate departureDate) {
        if (dob == null || departureDate == null) {
            return ADULT;
        }
        int age = Period.between(dob, departureDate).getYears();
        if (age < INFANT_MAX_AGE) {
            return INFANT;
        }
        if (age < CHILD_MAX_AGE) {
            return CHILD;
        }
        return ADULT;
    }

    public String getLabel() {
        return switch (this) {
            case ADULT -> "Adult";
            case CHILD -> "Child";
            case INFANT -> "Infant";
        };
    }
}
