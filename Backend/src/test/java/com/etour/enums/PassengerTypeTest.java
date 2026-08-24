package com.etour.enums;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Age banding drives what every passenger is charged, so the boundaries are
 * pinned here. A silent change to INFANT_MAX_AGE or CHILD_MAX_AGE would
 * otherwise only show up as wrong money on a real booking.
 *
 * Pure logic - no Spring context, no database, no mocks.
 */
@DisplayName("PassengerType age banding")
class PassengerTypeTest {

    private static final LocalDate DEPARTURE = LocalDate.of(2026, 6, 15);

    @Nested
    @DisplayName("boundaries")
    class Boundaries {

        @ParameterizedTest(name = "born {0} -> {1} on 2026-06-15")
        @CsvSource({
                // Infant: strictly under 2 on the departure date.
                "2026-06-15, INFANT",   // born on departure day, age 0
                "2025-06-15, INFANT",   // exactly 1
                "2024-06-16, INFANT",   // one day short of 2
                // Child: 2 up to but not including 12.
                "2024-06-15, CHILD",    // exactly 2 - first day out of infancy
                "2020-01-01, CHILD",    // 6
                "2014-06-16, CHILD",    // one day short of 12
                // Adult: 12 and over.
                "2014-06-15, ADULT",    // exactly 12 - first day of adult pricing
                "1990-05-10, ADULT",
        })
        void bandsByAgeOnDepartureDate(LocalDate dob, PassengerType expected) {
            assertThat(PassengerType.fromAge(dob, DEPARTURE)).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("incomplete data")
    class IncompleteData {

        @Test
        @DisplayName("a null date of birth falls back to ADULT, never to a cheaper band")
        void nullDobIsAdult() {
            // Documented legacy behaviour. It matters that the fallback is the
            // most expensive band - the opposite would let incomplete data
            // quietly under-charge.
            assertThat(PassengerType.fromAge(null, DEPARTURE)).isEqualTo(PassengerType.ADULT);
        }

        @Test
        @DisplayName("a null departure date falls back to ADULT")
        void nullDepartureIsAdult() {
            assertThat(PassengerType.fromAge(LocalDate.of(2020, 1, 1), null))
                    .isEqualTo(PassengerType.ADULT);
        }
    }

    @Test
    @DisplayName("a birthday later in the departure year has not happened yet")
    void birthdayNotYetReachedOnDeparture() {
        // Turns 12 in December, travelling in June - still a child for this trip.
        assertThat(PassengerType.fromAge(LocalDate.of(2014, 12, 1), DEPARTURE))
                .isEqualTo(PassengerType.CHILD);
    }

    @Test
    @DisplayName("every band exposes a human label for invoices and quotes")
    void labelsArePresent() {
        assertThat(PassengerType.ADULT.getLabel()).isEqualTo("Adult");
        assertThat(PassengerType.CHILD.getLabel()).isEqualTo("Child");
        assertThat(PassengerType.INFANT.getLabel()).isEqualTo("Infant");
    }
}
