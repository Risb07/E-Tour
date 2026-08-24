package com.etour.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.etour.dto.BookingAddonResponse;
import com.etour.dto.BookingQuoteResponse;
import com.etour.dto.BookingRequest;
import com.etour.dto.BookingResponse;
import com.etour.dto.PassengerInput;
import com.etour.entity.Booking;
import com.etour.entity.BookingAddon;
import com.etour.entity.Customer;
import com.etour.entity.Passenger;
import com.etour.entity.RoomCharge;
import com.etour.entity.Tour;
import com.etour.entity.TourAddon;
import com.etour.entity.TourCost;
import com.etour.entity.TourSchedule;
import com.etour.enums.BookingStatus;
import com.etour.enums.Occupancy;
import com.etour.enums.PassengerType;
import com.etour.exception.IllegalOperationException;
import com.etour.exception.ResourceNotFoundException;
import com.etour.repository.BookingAddonRepository;
import com.etour.repository.BookingRepository;
import com.etour.repository.PassengerRepository;
import com.etour.repository.RoomChargeRepository;
import com.etour.repository.TourAddonRepository;
import com.etour.repository.TourCostRepository;
import com.etour.repository.TourScheduleRepository;
import com.etour.security.CurrentUserProvider;
import com.etour.service.BookingService;
import com.etour.service.impl.TourPricingCalculator.PricingResult;
import com.etour.service.impl.TourPricingCalculator.ResolvedPassenger;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final TourScheduleRepository tourScheduleRepository;
    private final TourAddonRepository tourAddonRepository;
    private final BookingAddonRepository bookingAddonRepository;
    private final PassengerRepository passengerRepository;
    private final TourCostRepository tourCostRepository;
    private final RoomChargeRepository roomChargeRepository;
    private final TourPricingCalculator pricingCalculator;
    private final CurrentUserProvider currentUserProvider;

    public BookingServiceImpl(BookingRepository bookingRepository,
            TourScheduleRepository tourScheduleRepository,
            TourAddonRepository tourAddonRepository,
            BookingAddonRepository bookingAddonRepository,
            PassengerRepository passengerRepository,
            TourCostRepository tourCostRepository,
            RoomChargeRepository roomChargeRepository,
            TourPricingCalculator pricingCalculator,
            CurrentUserProvider currentUserProvider) {
        this.bookingRepository = bookingRepository;
        this.tourScheduleRepository = tourScheduleRepository;
        this.tourAddonRepository = tourAddonRepository;
        this.bookingAddonRepository = bookingAddonRepository;
        this.passengerRepository = passengerRepository;
        this.tourCostRepository = tourCostRepository;
        this.roomChargeRepository = roomChargeRepository;
        this.pricingCalculator = pricingCalculator;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest request) {

        // customer is NEVER taken from the request body - always the
        // authenticated principal. This is what closes the IDOR.
        Customer customer = currentUserProvider.currentCustomer();

        TourSchedule schedule = tourScheduleRepository.findByIdForUpdate(request.getScheduleId())
                .orElseThrow(() -> new ResourceNotFoundException("Tour schedule not found"));

        if (schedule.getAvailableSeats() < request.getNumberOfPassengers()) {
            throw new IllegalOperationException(
                    "Only " + schedule.getAvailableSeats() + " seat(s) left on this schedule");
        }

        boolean passengersProvided = request.getPassengers() != null && !request.getPassengers().isEmpty();
        if (passengersProvided && request.getPassengers().size() != request.getNumberOfPassengers()) {
            throw new IllegalOperationException(
                    "Passenger count (" + request.getPassengers().size()
                            + ") does not match numberOfPassengers (" + request.getNumberOfPassengers() + ")");
        }

        validateDeclaredComposition(request.getAdultCount(), request.getChildCount(),
                request.getNumberOfPassengers());
        if (passengersProvided) {
            validatePassengerComposition(request.getPassengers(), schedule.getDepartureDate(),
                    request.getAdultCount());
        }

        BigDecimal addonsTotal = BigDecimal.ZERO;
        List<TourAddon> addonCatalog = new ArrayList<>();
        for (BookingRequest.BookingAddonSelection sel : request.getAddons()) {
            // Scoped to this schedule's tour and to active add-ons only, so a
            // hand-crafted request can't attach another tour's add-on or one
            // that has been withdrawn. Pricing of a valid selection is
            // unchanged.
            TourAddon catalog = tourAddonRepository
                    .findByAddonIdAndTour_TourIdAndStatusTrue(sel.getAddonId(), schedule.getTour().getTourId())
                    .orElseThrow(() -> new ResourceNotFoundException("Add-on not found: " + sel.getAddonId()));
            addonCatalog.add(catalog);
            addonsTotal = addonsTotal.add(catalog.getPrice().multiply(BigDecimal.valueOf(sel.getQuantity())));
        }

        BigDecimal passengersTotal;
        PricingResult pricing = null;
        if (passengersProvided) {
            pricing = priceFor(schedule, request.getPassengers());
            passengersTotal = pricing.passengersTotal;
        } else {
            // No passenger details yet (e.g. cart checkout) - flat ESTIMATE,
            // to be corrected once passengers are finalized (see
            // finalizePassengers()) and re-checked at payment time.
            passengersTotal = schedule.getPrice().multiply(BigDecimal.valueOf(request.getNumberOfPassengers()));
        }
        BigDecimal total = passengersTotal.add(addonsTotal);

        Booking booking = new Booking();
        booking.setCustomer(customer);
        booking.setSchedule(schedule);
        booking.setBookingDate(LocalDate.now());
        booking.setBookingStatus(BookingStatus.PENDING);
        booking.setNumberOfPassengers(request.getNumberOfPassengers());
        // Remember the mix that was sold, so the passenger step can render the
        // right forms and the booking records what the customer chose.
        booking.setAdultCount(request.getAdultCount());
        booking.setChildCount(request.getChildCount());
        booking.setTotalAmount(total);

        Booking saved = bookingRepository.save(booking);

        List<BookingAddon> addonRows = new ArrayList<>();
        for (int i = 0; i < request.getAddons().size(); i++) {
            BookingRequest.BookingAddonSelection sel = request.getAddons().get(i);
            TourAddon catalog = addonCatalog.get(i);
            BookingAddon addonRow = new BookingAddon();
            addonRow.setBooking(saved);
            addonRow.setAddon(catalog);
            addonRow.setAddonName(catalog.getAddonName());
            addonRow.setPriceType(catalog.getPriceType());
            addonRow.setUnitPrice(catalog.getPrice());
            addonRow.setQuantity(sel.getQuantity());
            addonRow.setTotalAddonCost(catalog.getPrice().multiply(BigDecimal.valueOf(sel.getQuantity())));
            addonRows.add(addonRow);
        }
        bookingAddonRepository.saveAll(addonRows);

        List<Passenger> passengerRows = new ArrayList<>();
        if (passengersProvided) {
            passengerRows = buildPassengers(saved, request.getPassengers(), pricing);
            passengerRepository.saveAll(passengerRows);
        }

        // Hold the seats.
        schedule.setAvailableSeats(schedule.getAvailableSeats() - request.getNumberOfPassengers());
        tourScheduleRepository.save(schedule);

        return toResponse(saved, addonRows, passengerRows, pricing);
    }

    @Override
    public BookingQuoteResponse quote(BookingRequest request) {
        TourSchedule schedule = tourScheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new ResourceNotFoundException("Tour schedule not found"));

        if (request.getPassengers() == null || request.getPassengers().size() != request.getNumberOfPassengers()) {
            throw new IllegalOperationException("Provide one passenger entry (with date of birth) per seat to quote a price");
        }

        // Quote the same party the booking would create - including refusing
        // to price one that has no adult, so the review screen can't show a
        // total for a booking that will be rejected a step later.
        validateDeclaredComposition(request.getAdultCount(), request.getChildCount(),
                request.getNumberOfPassengers());
        validatePassengerComposition(request.getPassengers(), schedule.getDepartureDate(),
                request.getAdultCount());

        BigDecimal addonsTotal = BigDecimal.ZERO;
        for (BookingRequest.BookingAddonSelection sel : request.getAddons()) {
            // Same scoped lookup as createBooking, so the quote can't price an
            // add-on the booking would then refuse.
            TourAddon catalog = tourAddonRepository
                    .findByAddonIdAndTour_TourIdAndStatusTrue(sel.getAddonId(), schedule.getTour().getTourId())
                    .orElseThrow(() -> new ResourceNotFoundException("Add-on not found: " + sel.getAddonId()));
            addonsTotal = addonsTotal.add(catalog.getPrice().multiply(BigDecimal.valueOf(sel.getQuantity())));
        }

        PricingResult pricing = priceFor(schedule, request.getPassengers());
        BigDecimal total = pricing.passengersTotal.add(addonsTotal);

        BookingQuoteResponse response = new BookingQuoteResponse(schedule.getScheduleId(), pricing.roomSummary,
                pricing.breakdown, pricing.passengersTotal, addonsTotal, total);
        // Per-passenger name / type / occupancy / price, so the review screen
        // can show each traveller's own line instead of only grouped totals.
        response.setPassengerLines(pricing.passengerLines());
        return response;
    }

    @Override
    @Transactional
    public BookingResponse finalizePassengers(Long bookingId, List<PassengerInput> passengers) {
        Booking booking = requireOwnedOrAdmin(bookingId);

        if (booking.getBookingStatus() != BookingStatus.PENDING) {
            throw new IllegalOperationException("Only a pending booking can have passenger details added");
        }
        List<Passenger> existing = passengerRepository.findByBooking_BookingId(bookingId);
        if (!existing.isEmpty()) {
            throw new IllegalOperationException("Passenger details have already been added to this booking");
        }
        if (passengers == null || passengers.size() != booking.getNumberOfPassengers()) {
            throw new IllegalOperationException(
                    "Expected " + booking.getNumberOfPassengers() + " passenger(s), got "
                            + (passengers == null ? 0 : passengers.size()));
        }

        // The cart path reaches payment through here, so this is where a
        // cart-originated booking is held to the same composition rules.
        validatePassengerComposition(passengers, booking.getSchedule().getDepartureDate(),
                booking.getAdultCount());

        // Price first, then persist: the passenger rows store the resolved
        // category and the amount actually charged, which only the calculator
        // knows (an adult who chose nothing may be paired into a twin, a
        // child's category may come from the legacy needsExtraBed flag).
        PricingResult pricing = priceFor(booking.getSchedule(), passengers);
        List<Passenger> passengerRows = buildPassengers(booking, passengers, pricing);
        passengerRepository.saveAll(passengerRows);

        List<BookingAddon> addons = bookingAddonRepository.findByBooking_BookingId(bookingId);
        BigDecimal addonsTotal = addons.stream().map(BookingAddon::getTotalAddonCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        booking.setTotalAmount(pricing.passengersTotal.add(addonsTotal));
        Booking saved = bookingRepository.save(booking);

        return toResponse(saved, addons, passengerRows, pricing);
    }

    /**
     * Party-composition rules, applied wherever a booking is priced or created.
     *
     * A booking must have a lead adult. Without this a party of children (or a
     * single newborn) could be booked, and since infants are free and children
     * are discounted, the total could be zero - a tour would be sold for
     * nothing, with nobody old enough to travel responsible for it.
     *
     * Two independent checks, because either alone leaves a hole:
     *  1. The DECLARED composition from the tour page must be internally
     *     consistent and contain an adult.
     *  2. The passengers' DATES OF BIRTH must actually produce an adult, and
     *     each passenger must fall in the band their slot was sold as.
     *     Declaring "1 adult" and then entering a newborn's date of birth is
     *     exactly the bug this closes.
     *
     * Both are skipped when the information isn't present, so a request that
     * predates the composition fields (or a booking created before passenger
     * details are collected) still behaves as it did.
     */
    private void validateDeclaredComposition(Integer adultCount, Integer childCount, int numberOfPassengers) {
        if (adultCount == null && childCount == null) {
            return; // legacy request - nothing was declared
        }
        int adults = adultCount == null ? 0 : adultCount;
        int children = childCount == null ? 0 : childCount;

        if (adults + children != numberOfPassengers) {
            throw new IllegalOperationException(
                    "Adults (" + adults + ") plus children (" + children + ") must equal the number of passengers ("
                            + numberOfPassengers + ")");
        }
        if (adults < 1) {
            throw new IllegalOperationException("At least one adult is required for every booking.");
        }
    }

    /**
     * Checks the collected passengers against the declared composition, and
     * against the rule that somebody on the booking must be an adult.
     *
     * Slots are positional and match how the passenger form is rendered: the
     * first `adultCount` entries are the adult slots, the rest are the child
     * slots. An infant in a child slot is fine - both are simply "not an
     * adult" - but an adult date of birth in a child slot (or vice versa)
     * means the party being priced is not the party that was sold.
     */
    private void validatePassengerComposition(List<PassengerInput> passengers, LocalDate departureDate,
            Integer adultCount) {

        boolean hasAdult = false;
        for (int i = 0; i < passengers.size(); i++) {
            PassengerType band = PassengerType.fromAge(passengers.get(i).getDob(), departureDate);
            if (band == PassengerType.ADULT) {
                hasAdult = true;
            }

            if (adultCount == null) continue; // nothing declared to match against

            boolean isAdultSlot = i < adultCount;
            if (isAdultSlot && band != PassengerType.ADULT) {
                throw new IllegalOperationException(
                        "Passenger " + (i + 1) + " was booked as an adult but the date of birth given is a "
                                + band.getLabel().toLowerCase() + " on the departure date");
            }
            if (!isAdultSlot && band == PassengerType.ADULT) {
                throw new IllegalOperationException(
                        "Passenger " + (i + 1) + " was booked as a child but the date of birth given is an adult "
                                + "on the departure date");
            }
        }

        if (!hasAdult) {
            throw new IllegalOperationException("At least one adult is required for every booking.");
        }
    }

    private PricingResult priceFor(TourSchedule schedule, List<PassengerInput> passengers) {
        Tour tour = schedule.getTour();
        Optional<TourCost> cost = tourCostRepository
                .findFirstByTour_TourIdAndStatusAndValidFromLessThanEqualAndValidToGreaterThanEqualOrderByCostIdDesc(
                        tour.getTourId(), 1, schedule.getDepartureDate(), schedule.getDepartureDate());
        return pricingCalculator.calculate(passengers, schedule.getDepartureDate(), cost.orElse(null),
                schedule.getPrice(), roomChargesFor(tour.getTourId()));
    }

    /** Admin-configured room supplements for a tour, keyed by occupancy. */
    private Map<Occupancy, BigDecimal> roomChargesFor(Long tourId) {
        Map<Occupancy, BigDecimal> charges = new EnumMap<>(Occupancy.class);
        for (RoomCharge rc : roomChargeRepository.findByTour_TourIdAndActiveTrue(tourId)) {
            charges.put(rc.getOccupancy(), rc.getCharge());
        }
        return charges;
    }

    /**
     * Turns the submitted passenger inputs into rows attached to a booking,
     * stamping each one with the category and price the calculator resolved.
     *
     * The resolved values are used rather than the raw request because the
     * server has the last word: an adult who chose nothing may have been
     * paired into a twin room, a child's category may have come from the
     * legacy needsExtraBed flag, and an infant carries no category at all.
     */
    private List<Passenger> buildPassengers(Booking booking, List<PassengerInput> inputs, PricingResult pricing) {
        List<Passenger> rows = new ArrayList<>(inputs.size());
        for (int i = 0; i < inputs.size(); i++) {
            PassengerInput p = inputs.get(i);
            Passenger passenger = new Passenger();
            passenger.setBooking(booking);
            passenger.setFullName(p.getFullName());
            passenger.setGender(p.getGender());
            passenger.setDob(p.getDob());
            passenger.setNationality(p.getNationality());
            passenger.setIdProofType(p.getIdProofType());
            passenger.setIdProofNumber(p.getIdProofNumber());
            passenger.setNeedsExtraBed(p.getNeedsExtraBed());
            passenger.setAddressLine1(p.getAddressLine1());
            passenger.setAddressLine2(p.getAddressLine2());
            passenger.setCity(p.getCity());
            passenger.setState(p.getState());
            passenger.setCountry(p.getCountry());
            passenger.setPincode(p.getPincode());

            // Persist the resolved category AND the money. Storing the
            // amounts (rather than recomputing later) means a future admin
            // price change never rewrites what this customer actually paid.
            ResolvedPassenger resolved = pricing != null && i < pricing.passengers.size()
                    ? pricing.passengers.get(i)
                    : null;
            if (resolved != null) {
                passenger.setPassengerType(resolved.type);
                passenger.setOccupancy(resolved.occupancy);
                passenger.setRoomCharge(resolved.occupancy == null ? null : resolved.roomCharge);
                passenger.setPassengerPrice(resolved.price);
            } else {
                // Defensive only - every caller prices before building rows.
                passenger.setOccupancy(p.getOccupancy());
            }
            rows.add(passenger);
        }
        return rows;
    }

    @Override
    public BookingResponse getBookingById(Long bookingId) {
        Booking booking = requireOwnedOrAdmin(bookingId);
        return toResponse(booking, bookingAddonRepository.findByBooking_BookingId(bookingId),
                passengerRepository.findByBooking_BookingId(bookingId), null);
    }

    @Override
    public List<BookingResponse> getAllBookings() {
        // Admin-only route in SecurityConfig already restricts this,
        // but double-checking here keeps the service safe if the route
        // mapping ever changes.
        if (!currentUserProvider.isAdmin()) {
            throw new IllegalOperationException("Only admins can list all bookings");
        }
        return bookingRepository.findAll().stream()
                .map(b -> toResponse(b, bookingAddonRepository.findByBooking_BookingId(b.getBookingId()),
                        passengerRepository.findByBooking_BookingId(b.getBookingId()), null))
                .toList();
    }

    @Override
    public List<BookingResponse> getMyBookings() {
        Customer customer = currentUserProvider.currentCustomer();
        return bookingRepository.findByCustomer_CustomerId(customer.getCustomerId()).stream()
                .map(b -> toResponse(b, bookingAddonRepository.findByBooking_BookingId(b.getBookingId()),
                        passengerRepository.findByBooking_BookingId(b.getBookingId()), null))
                .toList();
    }

    @Override
    @Transactional
    public BookingResponse updateStatus(Long bookingId, BookingStatus status) {
        // Only admins change booking status directly (e.g. CONFIRMED after
        // payment capture). Customers cancel via cancelBooking().
        if (!currentUserProvider.isAdmin()) {
            throw new IllegalOperationException("Only admins can change booking status directly");
        }
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (booking.getBookingStatus() == BookingStatus.CANCELLED && status != BookingStatus.CANCELLED) {
            throw new IllegalOperationException("Cannot change status of a cancelled booking");
        }

        booking.setBookingStatus(status);
        Booking saved = bookingRepository.save(booking);
        return toResponse(saved, bookingAddonRepository.findByBooking_BookingId(bookingId),
                passengerRepository.findByBooking_BookingId(bookingId), null);
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId) {
        Booking booking = requireOwnedOrAdmin(bookingId);

        if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            throw new IllegalOperationException("Booking is already cancelled");
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        // Release the seats back to the schedule. numberOfPassengers is the
        // persisted count from booking creation - not derived from
        // totalAmount, which also includes add-on costs and would otherwise
        // under/over-count seats for any booking that included add-ons.
        TourSchedule schedule = booking.getSchedule();
        Integer passengers = booking.getNumberOfPassengers();
        if (passengers == null) {
            // Safety net only for booking rows that pre-date this column.
            passengers = 1;
        }
        schedule.setAvailableSeats(schedule.getAvailableSeats() + passengers);
        tourScheduleRepository.save(schedule);
    }

    private Booking requireOwnedOrAdmin(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (currentUserProvider.isAdmin()) {
            return booking;
        }

        Customer customer = currentUserProvider.currentCustomer();
        if (!booking.getCustomer().getCustomerId().equals(customer.getCustomerId())) {
            // 404 instead of 403 on purpose - don't confirm the booking
            // ID exists to a caller who doesn't own it.
            throw new ResourceNotFoundException("Booking not found");
        }
        return booking;
    }

    private BookingResponse toResponse(Booking booking, List<BookingAddon> addons, List<Passenger> passengers,
            PricingResult pricing) {
        List<BookingAddonResponse> addonDtos = addons.stream()
                .map(a -> new BookingAddonResponse(
                        a.getBookingAddonId(),
                        a.getAddon().getAddonId(),
                        a.getAddonName(),
                        a.getPriceType().name(),
                        a.getUnitPrice(),
                        a.getQuantity(),
                        a.getTotalAddonCost()))
                .toList();

        BookingResponse response = new BookingResponse(
                booking.getBookingId(),
                booking.getCustomer().getCustomerId(),
                booking.getCustomer().getFullName(),
                booking.getSchedule().getScheduleId(),
                booking.getSchedule().getTour().getTourId(),
                booking.getSchedule().getTour().getTitle(),
                booking.getSchedule().getDepartureDate(),
                booking.getBookingDate(),
                booking.getTotalAmount(),
                booking.getOrderNumber(),
                booking.getBookingStatus().name(),
                addonDtos);
        response.setNumberOfPassengers(booking.getNumberOfPassengers());
        response.setAdultCount(booking.getAdultCount());
        response.setChildCount(booking.getChildCount());
        response.setPassengersFinalized(!passengers.isEmpty());
        if (pricing != null) {
            response.setRoomSummary(pricing.roomSummary);
            response.setBreakdown(pricing.breakdown);
            response.setPassengerLines(pricing.passengerLines());
        }
        return response;
    }
}
