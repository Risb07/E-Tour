package com.etour.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.etour.dto.BookingAddonResponse;
import com.etour.dto.CardPaymentRequest;
import com.etour.dto.PassengerDto;
import com.etour.dto.PassengerInput;
import com.etour.dto.PaymentRequest;
import com.etour.dto.PaymentResponse;
import com.etour.dto.PaymentSummaryResponse;
import com.etour.entity.Booking;
import com.etour.entity.BookingAddon;
import com.etour.entity.Customer;
import com.etour.entity.Passenger;
import com.etour.entity.Payment;
import com.etour.enums.BookingStatus;
import com.etour.enums.Occupancy;
import com.etour.enums.PaymentStatus;
import com.etour.exception.IllegalOperationException;
import com.etour.exception.ResourceNotFoundException;
import com.etour.payment.CardDetails;
import com.etour.payment.GatewayResult;
import com.etour.payment.PaymentGateway;
import com.etour.repository.BookingAddonRepository;
import com.etour.repository.BookingRepository;
import com.etour.repository.PassengerRepository;
import com.etour.repository.PaymentRepository;
import com.etour.repository.TourCostRepository;
import com.etour.security.CurrentUserProvider;
import com.etour.service.PaymentService;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final PassengerRepository passengerRepository;
    private final CurrentUserProvider currentUserProvider;
    private final InvoiceServiceImpl invoiceService;
    private final ReceiptEmailService receiptEmailService;
    private final PaymentGateway paymentGateway;
    private final BookingAddonRepository bookingAddonRepository;
    private final TourCostRepository tourCostRepository;
    private final TourPricingCalculator pricingCalculator;
    private final BigDecimal gstRate;

    public PaymentServiceImpl(PaymentRepository paymentRepository, BookingRepository bookingRepository,
            PassengerRepository passengerRepository, CurrentUserProvider currentUserProvider,
            InvoiceServiceImpl invoiceService, ReceiptEmailService receiptEmailService,
            PaymentGateway paymentGateway, BookingAddonRepository bookingAddonRepository,
            TourCostRepository tourCostRepository, TourPricingCalculator pricingCalculator,
            @Value("${app.invoice.gst-rate:0.05}") BigDecimal gstRate) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.passengerRepository = passengerRepository;
        this.currentUserProvider = currentUserProvider;
        this.invoiceService = invoiceService;
        this.receiptEmailService = receiptEmailService;
        this.paymentGateway = paymentGateway;
        this.bookingAddonRepository = bookingAddonRepository;
        this.tourCostRepository = tourCostRepository;
        this.pricingCalculator = pricingCalculator;
        this.gstRate = gstRate;
    }

    @Override
    @Transactional
    public PaymentResponse recordPayment(PaymentRequest request) {
        Booking booking = requirePayableBooking(request.getBookingId());
        return charge(booking, request.getPaymentMethod(), null);
    }

    /**
     * BRD 3.7 card payment. Shares the entire confirmation path with
     * recordPayment() - ownership checks, status transition, order number,
     * invoice and receipt e-mail all live in charge(), so adding cards
     * introduced no duplicate business logic.
     */
    @Override
    @Transactional
    public PaymentResponse payWithCard(CardPaymentRequest request) {
        Booking booking = requirePayableBooking(request.getBookingId());

        CardDetails card = new CardDetails(
                request.getCardNumber(),
                request.getCardHolderName(),
                request.getExpiryMonth(),
                request.getExpiryYear(),
                request.getCvv());

        // Fail fast on obviously bad input before involving the gateway. The
        // gateway re-checks these too - the client is never the only gate.
        if (!card.passesLuhn()) {
            throw new IllegalOperationException("The card number is not valid");
        }
        if (card.isExpired(YearMonth.now())) {
            throw new IllegalOperationException("The card has expired");
        }
        if (!card.hasValidCvv()) {
            throw new IllegalOperationException("The security code is not valid");
        }

        return charge(booking, request.getPaymentMethod(), card);
    }

    /** The pre-tax subtotal held on the booking. Null-safe. */
    private BigDecimal subTotalOf(Booking booking) {
        return booking.getTotalAmount() == null ? BigDecimal.ZERO : booking.getTotalAmount();
    }

    /**
     * GST on a taxable amount, rounded the same way InvoiceServiceImpl rounds
     * it, so the tax on the payment page, the tax charged and the tax on the
     * invoice are the same number to the paisa.
     */
    private BigDecimal gstOn(BigDecimal taxable) {
        return taxable.multiply(gstRate).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * What the customer actually owes: subtotal plus GST.
     *
     * This is the single definition of "the amount", used for the gateway
     * charge, the stored payment amount and the figure shown on the payment
     * page. It deliberately matches InvoiceServiceImpl#generate, which derives
     * the invoice total the same way from the same gst-rate property - so the
     * invoice total and the payment amount always agree.
     */
    private BigDecimal payableAmount(Booking booking) {
        BigDecimal subTotal = subTotalOf(booking);
        return subTotal.add(gstOn(subTotal));
    }

    /**
     * Loads a booking the current customer is allowed to pay for, and checks
     * it is actually in a payable state.
     */
    private Booking requirePayableBooking(Long bookingId) {
        Customer customer = currentUserProvider.currentCustomer();

        // Locked, not just read: both callers are @Transactional, so the lock
        // is held from here through the gateway call and the status change.
        // A second concurrent attempt blocks until the first commits, then
        // sees CONFIRMED and is refused below - instead of both reading
        // PENDING and both charging the card.
        Booking booking = bookingRepository.findByIdForUpdate(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.getCustomer().getCustomerId().equals(customer.getCustomerId())) {
            // 404 not 403 - don't confirm the booking exists to a non-owner.
            throw new ResourceNotFoundException("Booking not found");
        }
        if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            throw new IllegalOperationException("Cannot pay for a cancelled booking");
        }
        if (booking.getBookingStatus() == BookingStatus.CONFIRMED) {
            throw new IllegalOperationException("This booking is already paid and confirmed");
        }

        // BRD 3.7 - passenger details (and, for cart-originated bookings, the
        // recalculated banded price) must be finalized before payment is
        // accepted. See BookingService.finalizePassengers().
        long passengerCount = passengerRepository.findByBooking_BookingId(booking.getBookingId()).size();
        if (booking.getNumberOfPassengers() != null && passengerCount != booking.getNumberOfPassengers()) {
            throw new IllegalOperationException(
                    "Passenger details are incomplete for this booking - add them via "
                            + "PATCH /api/bookings/" + booking.getBookingId() + "/passengers before paying");
        }
        return booking;
    }

    /**
     * Single confirmation path for every payment method. The charge itself is
     * delegated to whichever PaymentGateway bean is active (simulated by
     * default); this service keeps the domain rules and knows nothing about
     * any specific provider.
     */
    private PaymentResponse charge(Booking booking, String paymentMethod, CardDetails card) {

        // The amount actually payable - the pre-tax subtotal PLUS GST. This is
        // the figure the payment page shows on the Pay button (grandTotal) and
        // the figure the invoice records as its total, so it is the figure that
        // must be charged. Previously the raw subtotal was sent to the gateway
        // and stored on the payment, which under-charged every booking by the
        // GST rate and left payment.amount != invoice.total_amount.
        BigDecimal payable = payableAmount(booking);

        GatewayResult result = paymentGateway.charge(new PaymentGateway.GatewayChargeRequest(
                booking.getBookingId(),
                payable,
                paymentMethod,
                booking.getCustomer().getEmail(),
                card));

        // A declined payment must not confirm the booking or produce an invoice.
        // Checked BEFORE persisting: this method is @Transactional, so throwing
        // after a save would roll that row back anyway - persisting failed
        // attempts for audit needs a separate REQUIRES_NEW transaction, which
        // is worth adding alongside a real (asynchronous) gateway.
        if (!result.isSuccessful()) {
            log.warn("Payment declined for booking {} via {}: status={} reason={}",
                    booking.getBookingId(), paymentGateway.getProviderName(),
                    result.getStatus(), result.getFailureReason());
            throw new IllegalOperationException(
                    result.getFailureReason() == null
                            ? "Payment was not successful. Please try again."
                            : "Payment failed: " + result.getFailureReason());
        }

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(payable);
        payment.setPaymentMethod(paymentMethod);
        payment.setPaymentStatus(result.getStatus());
        payment.setTransactionRef(result.getTransactionRef());
        payment.setGateway(paymentGateway.getProviderName());
        // Brand + last four only. The PAN and CVV are discarded with the
        // request - they are never written to the database.
        payment.setCardSummary(card == null ? null : card.getMaskedSummary());

        Payment saved = paymentRepository.save(payment);

        // BRD 3.7 - unique order number generated once payment succeeds. The
        // receipt (invoice) is then presented/e-mailed against this number.
        booking.setOrderNumber(generateOrderNumber(booking.getBookingId()));
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        var invoice = invoiceService.generate(booking, saved);

        // BRD 3.7 / Tour Page - receipt is generated as a PDF and e-mailed.
        // Best-effort: a broken mail server shouldn't roll back a successful
        // payment, so failures here are logged, not thrown.
        receiptEmailService.sendReceiptEmail(booking, invoice);

        return toDto(saved);
    }

    /**
     * Everything the payment page needs, computed from the PERSISTED booking
     * so the figure shown is exactly what will be charged. The client never
     * calculates or supplies the amount.
     */
    @Override
    public PaymentSummaryResponse getPaymentSummary(Long bookingId) {
        Customer customer = currentUserProvider.currentCustomer();

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        if (!booking.getCustomer().getCustomerId().equals(customer.getCustomerId())) {
            throw new ResourceNotFoundException("Booking not found");
        }

        List<Passenger> passengers = passengerRepository.findByBooking_BookingId(bookingId);
        List<BookingAddon> addons = bookingAddonRepository.findByBooking_BookingId(bookingId);

        PaymentSummaryResponse dto = new PaymentSummaryResponse();
        dto.setBookingId(booking.getBookingId());
        dto.setBookingStatus(booking.getBookingStatus().name());
        dto.setOrderNumber(booking.getOrderNumber());
        dto.setTourId(booking.getSchedule().getTour().getTourId());
        dto.setTourTitle(booking.getSchedule().getTour().getTitle());
        dto.setDepartureDate(booking.getSchedule().getDepartureDate());
        dto.setReturnDate(booking.getSchedule().getReturnDate());
        dto.setNumberOfPassengers(booking.getNumberOfPassengers());
        dto.setPassengers(passengers.stream().map(this::toPassengerDto).toList());
        dto.setAddons(addons.stream()
                .map(a -> new BookingAddonResponse(a.getBookingAddonId(), a.getAddon().getAddonId(),
                        a.getAddonName(), a.getPriceType().name(), a.getUnitPrice(), a.getQuantity(),
                        a.getTotalAddonCost()))
                .toList());

        // Re-run the same pricing engine used at booking time so the room
        // requirement and per-band lines shown here match the stored total.
        if (!passengers.isEmpty()) {
            List<PassengerInput> inputs = passengers.stream().map(this::toPassengerInput).toList();
            Long tourId = booking.getSchedule().getTour().getTourId();

            // Uses the charge STORED on each passenger, not today's admin
            // price - so a later price change never rewrites what an existing
            // customer was quoted.
            Map<Occupancy, BigDecimal> storedRoomCharges = new EnumMap<>(Occupancy.class);
            for (Passenger p : passengers) {
                if (p.getOccupancy() != null && p.getRoomCharge() != null) {
                    storedRoomCharges.putIfAbsent(p.getOccupancy(), p.getRoomCharge());
                }
            }

            TourPricingCalculator.PricingResult pricing = pricingCalculator.calculate(
                    inputs,
                    booking.getSchedule().getDepartureDate(),
                    tourCostRepository
                            .findFirstByTour_TourIdAndStatusAndValidFromLessThanEqualAndValidToGreaterThanEqualOrderByCostIdDesc(
                                    tourId, 1,
                                    booking.getSchedule().getDepartureDate(),
                                    booking.getSchedule().getDepartureDate())
                            .orElse(null),
                    booking.getSchedule().getPrice(),
                    storedRoomCharges);
            dto.setRoomSummary(pricing.roomSummary);
            dto.setBreakdown(pricing.breakdown);
        }

        // booking.totalAmount is the pre-tax subtotal; tax is added on the
        // invoice, so the same arithmetic is mirrored here - via the same
        // helpers charge() uses, so what is displayed and what is charged
        // cannot drift apart.
        BigDecimal subTotal = subTotalOf(booking);
        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal taxable = subTotal.subtract(discount);
        BigDecimal tax = gstOn(taxable);

        dto.setSubTotal(subTotal);
        dto.setDiscountAmount(discount);
        dto.setTaxAmount(tax);
        dto.setGstRatePercent(gstRate.multiply(BigDecimal.valueOf(100)));
        dto.setGrandTotal(taxable.add(tax));

        dto.setAlreadyPaid(paymentRepository.findByBooking_BookingId(bookingId).stream()
                .anyMatch(p -> p.getPaymentStatus() == PaymentStatus.SUCCESS));

        return dto;
    }

    private PassengerDto toPassengerDto(Passenger p) {
        PassengerDto d = new PassengerDto();
        d.setPassengerId(p.getPassengerId());
        d.setBookingId(p.getBooking().getBookingId());
        d.setFullName(p.getFullName());
        d.setGender(p.getGender());
        d.setDob(p.getDob());
        d.setNationality(p.getNationality());
        d.setIdProofType(p.getIdProofType());
        d.setIdProofNumber(p.getIdProofNumber());
        d.setNeedsExtraBed(p.getNeedsExtraBed());
        d.setPassengerType(p.getPassengerType() == null ? null : p.getPassengerType().name());
        d.setOccupancy(p.getOccupancy() == null ? null : p.getOccupancy().name());
        d.setRoomCharge(p.getRoomCharge());
        d.setPassengerPrice(p.getPassengerPrice());
        d.setAddressLine1(p.getAddressLine1());
        d.setAddressLine2(p.getAddressLine2());
        d.setCity(p.getCity());
        d.setState(p.getState());
        d.setCountry(p.getCountry());
        d.setPincode(p.getPincode());
        return d;
    }

    /** Rebuilds the pricing input from a persisted passenger row. */
    private PassengerInput toPassengerInput(Passenger p) {
        PassengerInput input = new PassengerInput();
        input.setFullName(p.getFullName());
        input.setGender(p.getGender());
        input.setDob(p.getDob());
        input.setNeedsExtraBed(p.getNeedsExtraBed());
        // Only the occupancy is carried across. The band is re-derived from
        // the date of birth by the calculator - the same rule that produced
        // the stored passenger_type in the first place - so passing the
        // stored type would be redundant, and passing it would not be honoured
        // anyway.
        input.setOccupancy(p.getOccupancy());
        return input;
    }

    private String generateOrderNumber(Long bookingId) {
        String suffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "ORD-" + bookingId + "-" + suffix;
    }

    @Override
    public PaymentResponse getPayment(Long paymentId) {
        Customer customer = currentUserProvider.isAdmin() ? null : currentUserProvider.currentCustomer();

        Payment payment;
        if (customer == null) {
            payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        } else {
            payment = paymentRepository.findByPaymentIdAndBooking_Customer_CustomerId(paymentId, customer.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        }
        return toDto(payment);
    }

    private PaymentResponse toDto(Payment p) {
        PaymentResponse dto = new PaymentResponse(p.getPaymentId(), p.getBooking().getBookingId(), p.getAmount(),
                p.getPaymentMethod(), p.getPaymentStatus().name(), p.getTransactionRef(), p.getCreatedAt());
        dto.setGateway(p.getGateway());
        dto.setCardSummary(p.getCardSummary());
        dto.setUpdatedAt(p.getUpdatedAt());
        dto.setOrderNumber(p.getBooking().getOrderNumber());
        return dto;
    }
}
