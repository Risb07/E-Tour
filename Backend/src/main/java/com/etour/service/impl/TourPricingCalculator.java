package com.etour.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.etour.dto.CostBreakdownLine;
import com.etour.dto.PassengerInput;
import com.etour.dto.PassengerPriceLine;
import com.etour.dto.RoomSummary;
import com.etour.entity.TourCost;
import com.etour.enums.Occupancy;
import com.etour.enums.PassengerType;
import com.etour.exception.IllegalOperationException;

/**
 * BRD 3.7 "Book Tour" pricing.
 *
 * Every passenger is priced individually from two choices: their TYPE
 * (adult / child / infant) and, for the first two, their OCCUPANCY CATEGORY.
 * The category selects a column on the tour's TourCost sheet:
 *
 *   Adult  TWIN               -> basePrice            (the default tour cost)
 *          SINGLE             -> singlePersonCost
 *          EXTRA_BED, TRIPLE  -> extraPersonCost
 *   Child  CHILD_WITH_BED     -> childWithBedCost
 *          CHILD_WITHOUT_BED  -> childWithoutBedCost
 *   Infant                    -> free, no bed, no room
 *
 * Any admin-configured RoomCharge supplement for the chosen category is added
 * on top of that rate, exactly as before.
 *
 * DATE OF BIRTH IS THE SINGLE SOURCE OF TRUTH for the band. Age at the
 * departure date decides it, using the boundaries this class has always used
 * (infant &lt; 2, child 2-12, adult 12+, null dob = adult). Any passengerType
 * on the request is ignored - a caller cannot talk the server into pricing a
 * six-year-old as an adult - and an occupancy that contradicts the derived
 * band is rejected rather than quietly repriced, so an invalid passenger can
 * never be saved.
 *
 * Backward compatibility (the cart-checkout path and older clients post
 * passengers without these fields):
 *  - occupancy omitted for adults -> the legacy pairing rule still applies:
 *    adults pair into twin rooms and a left-over solo adult pays
 *    singlePersonCost.
 *  - occupancy omitted for children -> the legacy needsExtraBed flag decides
 *    with-bed vs without-bed.
 *  - If no TourCost row is configured for the tour/date, falls back to the
 *    schedule's flat per-seat price for every adult, half that for children,
 *    and free for infants, with no single-room surcharge.
 */
@Component
public class TourPricingCalculator {

    /**
     * One passenger after the server has decided how they are actually being
     * priced. Carries the enums (so the booking can be persisted from it) as
     * well as the money (so nothing has to be recomputed downstream).
     */
    public static class ResolvedPassenger {
        public final int index;
        public final String fullName;
        public final PassengerType type;
        /** Null for infants. */
        public final Occupancy occupancy;
        public final BigDecimal categoryRate;
        public final BigDecimal roomCharge;
        public final BigDecimal price;

        ResolvedPassenger(int index, String fullName, PassengerType type, Occupancy occupancy,
                BigDecimal categoryRate, BigDecimal roomCharge, BigDecimal price) {
            this.index = index;
            this.fullName = fullName;
            this.type = type;
            this.occupancy = occupancy;
            this.categoryRate = categoryRate;
            this.roomCharge = roomCharge;
            this.price = price;
        }

        public PassengerPriceLine toPriceLine() {
            return new PassengerPriceLine(index, fullName, type, occupancy, categoryRate, roomCharge, price);
        }
    }

    public static class PricingResult {
        public final RoomSummary roomSummary;
        public final List<CostBreakdownLine> breakdown;
        public final BigDecimal passengersTotal;
        /** Per-passenger detail, in submitted order. Never null. */
        public final List<ResolvedPassenger> passengers;

        public PricingResult(RoomSummary roomSummary, List<CostBreakdownLine> breakdown, BigDecimal passengersTotal) {
            this(roomSummary, breakdown, passengersTotal, List.of());
        }

        public PricingResult(RoomSummary roomSummary, List<CostBreakdownLine> breakdown, BigDecimal passengersTotal,
                List<ResolvedPassenger> passengers) {
            this.roomSummary = roomSummary;
            this.breakdown = breakdown;
            this.passengersTotal = passengersTotal;
            this.passengers = passengers;
        }

        public List<PassengerPriceLine> passengerLines() {
            return passengers.stream().map(ResolvedPassenger::toPriceLine).toList();
        }
    }

    public PricingResult calculate(List<PassengerInput> passengers, LocalDate departureDate,
            TourCost cost, BigDecimal fallbackPerSeatPrice) {
        return calculate(passengers, departureDate, cost, fallbackPerSeatPrice, Map.of());
    }

    /**
     * @param roomCharges admin-configured per-occupancy supplements for this
     *   tour, keyed by Occupancy. A supplement is only applied when the client
     *   explicitly chose that occupancy - never when the server inferred it -
     *   so tours relying on the legacy pairing rule price exactly as before.
     */
    public PricingResult calculate(List<PassengerInput> passengers, LocalDate departureDate,
            TourCost cost, BigDecimal fallbackPerSeatPrice, Map<Occupancy, BigDecimal> roomCharges) {

        // --- Pass 1: decide each passenger's type and occupancy category ----

        int count = passengers.size();
        PassengerType[] types = new PassengerType[count];
        Occupancy[] occupancies = new Occupancy[count];
        // Whether the CLIENT chose the category (vs the server inferring it).
        // Only explicit choices attract a room supplement.
        boolean[] explicit = new boolean[count];

        int adults = 0;
        int children = 0;
        int infants = 0;
        boolean anyExplicitAdultOccupancy = false;

        for (int i = 0; i < count; i++) {
            PassengerInput p = passengers.get(i);
            // Date of birth decides the band, always. Whatever passengerType
            // the request carried is ignored on purpose - honouring it would
            // make the category a client-controlled input again.
            PassengerType type = PassengerType.fromAge(p.getDob(), departureDate);
            types[i] = type;

            Occupancy requested = p.getOccupancy();
            if (requested != null && !requested.appliesTo(type)) {
                // The form only ever offers categories valid for the derived
                // band, so reaching here means a stale payload or a
                // hand-crafted request. Refuse rather than silently pricing
                // something other than what was asked for.
                throw new IllegalOperationException(
                        "Passenger " + (i + 1) + " (" + safeName(p) + ") is a "
                                + type.getLabel().toLowerCase() + " by date of birth and cannot use the \""
                                + requested.getLabel() + "\" category");
            }

            switch (type) {
                case INFANT -> {
                    infants++;
                    occupancies[i] = null; // free, and occupies no bed
                }
                // Past the guard above, a non-null `requested` is known to be
                // valid for this band, so it can be taken as-is.
                case CHILD -> {
                    children++;
                    if (requested != null) {
                        occupancies[i] = requested;
                        explicit[i] = true;
                    } else {
                        occupancies[i] = Occupancy.defaultFor(type, p.getNeedsExtraBed());
                    }
                }
                case ADULT -> {
                    adults++;
                    if (requested != null) {
                        occupancies[i] = requested;
                        explicit[i] = true;
                        anyExplicitAdultOccupancy = true;
                    }
                    // else: left null for now - resolved in pass 2, because it
                    // depends on whether ANY adult stated a preference.
                }
            }
        }

        // --- Pass 2: fill in the adults nobody chose a category for ---------

        if (anyExplicitAdultOccupancy) {
            // At least one traveller expressed a preference, so honour the
            // selections and treat silence as twin sharing.
            for (int i = 0; i < count; i++) {
                if (types[i] == PassengerType.ADULT && occupancies[i] == null) {
                    occupancies[i] = Occupancy.TWIN;
                }
            }
        } else {
            // Legacy/auto behaviour: pair adults into twins, odd one out pays
            // the single supplement. Materialising the assignment per
            // passenger (rather than just counting) gives the same totals as
            // before while still producing a per-passenger breakdown.
            int assigned = 0;
            int adultsInTwins = (adults / 2) * 2;
            for (int i = 0; i < count; i++) {
                if (types[i] != PassengerType.ADULT) continue;
                occupancies[i] = assigned < adultsInTwins ? Occupancy.TWIN : Occupancy.SINGLE;
                assigned++;
            }
        }

        // --- Pass 3: rates ---------------------------------------------------

        BigDecimal adultTwinRate;
        BigDecimal adultSingleRate;
        BigDecimal adultExtraRate;
        BigDecimal childWithBedRate;
        BigDecimal childWithoutBedRate;

        if (cost != null) {
            adultTwinRate = nonNull(cost.getBasePrice(), fallbackPerSeatPrice);
            adultSingleRate = nonNull(cost.getSinglePersonCost(), adultTwinRate);
            adultExtraRate = nonNull(cost.getExtraPersonCost(), adultTwinRate);
            childWithBedRate = nonNull(cost.getChildWithBedCost(), half(fallbackPerSeatPrice));
            childWithoutBedRate = nonNull(cost.getChildWithoutBedCost(), half(fallbackPerSeatPrice));
        } else {
            adultTwinRate = fallbackPerSeatPrice;
            adultSingleRate = fallbackPerSeatPrice; // no configured single-room surcharge available
            adultExtraRate = fallbackPerSeatPrice;
            childWithBedRate = half(fallbackPerSeatPrice);
            childWithoutBedRate = half(fallbackPerSeatPrice);
        }

        // --- Pass 4: price each passenger, and tally the grouped view -------

        int twinAdults = 0;
        int singleAdults = 0;
        int extraBedAdults = 0;
        int childrenWithBed = 0;
        int childrenWithoutBed = 0;
        Map<Occupancy, Integer> supplementCounts = new EnumMap<>(Occupancy.class);

        List<ResolvedPassenger> resolved = new ArrayList<>(count);
        BigDecimal total = BigDecimal.ZERO;

        for (int i = 0; i < count; i++) {
            Occupancy occupancy = occupancies[i];
            // Pass 2 guarantees every adult has a category by now; the null
            // guard keeps this total-safe rather than throwing if that ever
            // stops being true.
            BigDecimal categoryRate = switch (types[i]) {
                case INFANT -> BigDecimal.ZERO;
                case CHILD -> occupancy == Occupancy.CHILD_WITHOUT_BED ? childWithoutBedRate : childWithBedRate;
                case ADULT -> {
                    if (occupancy == Occupancy.SINGLE) yield adultSingleRate;
                    if (occupancy == Occupancy.TRIPLE || occupancy == Occupancy.EXTRA_BED) yield adultExtraRate;
                    yield adultTwinRate;
                }
            };

            BigDecimal supplement = BigDecimal.ZERO;
            if (explicit[i] && occupancy != null) {
                BigDecimal configured = roomCharges.get(occupancy);
                if (configured != null && configured.compareTo(BigDecimal.ZERO) > 0) {
                    supplement = configured;
                    supplementCounts.merge(occupancy, 1, Integer::sum);
                }
            }

            BigDecimal price = categoryRate.add(supplement);
            total = total.add(price);
            resolved.add(new ResolvedPassenger(i, safeName(passengers.get(i)), types[i], occupancy,
                    categoryRate, supplement, price.setScale(2, RoundingMode.HALF_UP)));

            switch (types[i]) {
                case INFANT -> { /* counted above, contributes nothing */ }
                case CHILD -> {
                    if (occupancy == Occupancy.CHILD_WITHOUT_BED) childrenWithoutBed++; else childrenWithBed++;
                }
                case ADULT -> {
                    if (occupancy == Occupancy.SINGLE) singleAdults++;
                    else if (occupancy == Occupancy.TRIPLE || occupancy == Occupancy.EXTRA_BED) extraBedAdults++;
                    else twinAdults++;
                }
            }
        }

        // --- Pass 5: the grouped breakdown, unchanged in shape and order ----

        List<CostBreakdownLine> lines = new ArrayList<>();

        if (twinAdults > 0) {
            lines.add(line("Adults (twin-sharing)", adultTwinRate, twinAdults));
        }
        if (singleAdults > 0) {
            lines.add(line("Single occupancy", adultSingleRate, singleAdults));
        }
        if (extraBedAdults > 0) {
            lines.add(line("Extra person", adultExtraRate, extraBedAdults));
        }
        if (childrenWithBed > 0) {
            lines.add(line("Child (with bed)", childWithBedRate, childrenWithBed));
        }
        if (childrenWithoutBed > 0) {
            lines.add(line("Child (without bed)", childWithoutBedRate, childrenWithoutBed));
        }
        if (infants > 0) {
            lines.add(new CostBreakdownLine("Infant", BigDecimal.ZERO, infants, BigDecimal.ZERO));
        }
        // Admin-configured room supplements, one line per occupancy so the
        // customer can see exactly what the room choice costs. Zero charges
        // never reach here - they'd be a meaningless "+ 0.00" row.
        for (Map.Entry<Occupancy, Integer> entry : supplementCounts.entrySet()) {
            lines.add(line(entry.getKey().getLabel() + " supplement", roomCharges.get(entry.getKey()),
                    entry.getValue()));
        }

        // Twin-sharers pair up; an unpaired twin selection still needs a room,
        // so round up rather than losing it.
        int doubleRooms = (twinAdults + 1) / 2;
        RoomSummary roomSummary = new RoomSummary(adults, children, infants, doubleRooms, singleAdults,
                extraBedAdults);

        return new PricingResult(roomSummary, lines, total.setScale(2, RoundingMode.HALF_UP), resolved);
    }

    private CostBreakdownLine line(String label, BigDecimal unitPrice, int quantity) {
        return new CostBreakdownLine(label, unitPrice, quantity, unitPrice.multiply(BigDecimal.valueOf(quantity)));
    }

    private String safeName(PassengerInput p) {
        return p.getFullName() == null ? "" : p.getFullName();
    }

    private BigDecimal nonNull(BigDecimal value, BigDecimal fallback) {
        return value != null ? value : fallback;
    }

    private BigDecimal half(BigDecimal value) {
        return value.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
    }
}
