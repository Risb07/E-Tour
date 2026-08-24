using eTour.Application.Common;
using eTour.Application.Dtos;
using eTour.Application.Gateways;
using eTour.Application.Pricing;
using eTour.Domain.Entities;
using eTour.Domain.Enums;
using eTour.Domain.Exceptions;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Logging;

namespace eTour.Application.Services;

public class PaymentService : IPaymentService
{
    private readonly decimal _gstRate;
    private readonly IBookingLockRepository _bookingLockRepository;
    private readonly IGenericRepository<Booking, long> _bookingRepository;
    private readonly IGenericRepository<Payment, long> _paymentRepository;
    private readonly IGenericRepository<Passenger, long> _passengerRepository;
    private readonly IGenericRepository<BookingAddon, long> _bookingAddonRepository;
    private readonly IGenericRepository<TourCost, long> _tourCostRepository;
    private readonly ICurrentUserService _currentUser;
    private readonly IInvoiceService _invoiceService;
    private readonly IReceiptPdfService _receiptPdfService;
    private readonly IReceiptEmailService _receiptEmailService;
    private readonly IPaymentGateway _paymentGateway;
    private readonly TourPricingCalculator _pricingCalculator;
    private readonly ILogger<PaymentService> _logger;

    public PaymentService(IBookingLockRepository bookingLockRepository, IGenericRepository<Booking, long> bookingRepository,
        IGenericRepository<Payment, long> paymentRepository, IGenericRepository<Passenger, long> passengerRepository,
        IGenericRepository<BookingAddon, long> bookingAddonRepository, IGenericRepository<TourCost, long> tourCostRepository,
        ICurrentUserService currentUser, IInvoiceService invoiceService, IReceiptPdfService receiptPdfService,
        IReceiptEmailService receiptEmailService, IPaymentGateway paymentGateway, TourPricingCalculator pricingCalculator,
        IConfiguration configuration, ILogger<PaymentService> logger)
    {
        _bookingLockRepository = bookingLockRepository;
        _bookingRepository = bookingRepository;
        _paymentRepository = paymentRepository;
        _passengerRepository = passengerRepository;
        _bookingAddonRepository = bookingAddonRepository;
        _tourCostRepository = tourCostRepository;
        _currentUser = currentUser;
        _invoiceService = invoiceService;
        _receiptPdfService = receiptPdfService;
        _receiptEmailService = receiptEmailService;
        _paymentGateway = paymentGateway;
        _pricingCalculator = pricingCalculator;
        _logger = logger;
        _gstRate = configuration.GetValue<decimal?>("Invoice:GstRate") ?? 0.05m;
    }

    public async Task<PaymentResponse> RecordPaymentAsync(PaymentRequest request, CancellationToken ct = default)
    {
        var customer = await _currentUser.CurrentCustomerAsync(ct);
        return await _bookingLockRepository.WithLockAsync(request.BookingId, async booking =>
        {
            EnsurePayable(booking, customer.CustomerId, out var validBooking);
            return await ChargeAsync(validBooking, request.PaymentMethod, null, ct);
        }, ct);
    }

    /// <summary>
    /// Shares the entire confirmation path with RecordPaymentAsync - ownership checks, status
    /// transition, order number, invoice and receipt e-mail all live in ChargeAsync, so cards
    /// introduce no duplicate business logic.
    /// </summary>
    public async Task<PaymentResponse> PayWithCardAsync(CardPaymentRequest request, CancellationToken ct = default)
    {
        var customer = await _currentUser.CurrentCustomerAsync(ct);

        var card = new CardDetails(request.CardNumber, request.CardHolderName, request.ExpiryMonth, request.ExpiryYear, request.Cvv);

        // Fail fast on obviously bad input before involving the gateway. The gateway re-checks
        // these too - the client is never the only gate.
        if (!card.PassesLuhn())
        {
            throw new IllegalOperationException("The card number is not valid");
        }
        if (card.IsExpired(DateOnly.FromDateTime(DateTime.UtcNow)))
        {
            throw new IllegalOperationException("The card has expired");
        }
        if (!card.HasValidCvv())
        {
            throw new IllegalOperationException("The security code is not valid");
        }

        return await _bookingLockRepository.WithLockAsync(request.BookingId, async booking =>
        {
            EnsurePayable(booking, customer.CustomerId, out var validBooking);
            return await ChargeAsync(validBooking, request.PaymentMethod, card, ct);
        }, ct);
    }

    private void EnsurePayable(Booking? booking, long customerId, out Booking validBooking)
    {
        if (booking is null)
        {
            throw new ResourceNotFoundException("Booking not found");
        }
        // 404 not 403 - don't confirm the booking exists to a non-owner.
        if (booking.CustomerId != customerId)
        {
            throw new ResourceNotFoundException("Booking not found");
        }
        if (booking.BookingStatus == BookingStatus.CANCELLED)
        {
            throw new IllegalOperationException("Cannot pay for a cancelled booking");
        }
        if (booking.BookingStatus == BookingStatus.CONFIRMED)
        {
            throw new IllegalOperationException("This booking is already paid and confirmed");
        }

        // Passenger details (and, for cart-originated bookings, the recalculated banded price)
        // must be finalized before payment is accepted.
        var passengerCount = _passengerRepository.Query().Count(p => p.BookingId == booking.BookingId);
        if (booking.NumberOfPassengers is not null && passengerCount != booking.NumberOfPassengers)
        {
            throw new IllegalOperationException(
                $"Passenger details are incomplete for this booking - add them via PATCH /api/bookings/{booking.BookingId}/passengers before paying");
        }

        validBooking = booking;
    }

    /// <summary>The pre-tax subtotal held on the booking, plus GST - the single definition of "the amount".</summary>
    private decimal PayableAmount(Booking booking) => booking.TotalAmount + GstOn(booking.TotalAmount);

    private decimal GstOn(decimal taxable) => Math.Round(taxable * _gstRate, 2, MidpointRounding.AwayFromZero);

    private async Task<PaymentResponse> ChargeAsync(Booking booking, string paymentMethod, CardDetails? card, CancellationToken ct)
    {
        // The figure the payment page shows AND the figure charged - previously the raw
        // (pre-tax) subtotal was sent to the gateway, under-charging every booking by the GST rate.
        var payable = PayableAmount(booking);

        var result = _paymentGateway.Charge(new GatewayChargeRequest(booking.BookingId, payable, paymentMethod,
            CustomerEmailFor(booking), card));

        if (!result.IsSuccessful)
        {
            _logger.LogWarning("Payment declined for booking {BookingId} via {Provider}: status={Status} reason={Reason}",
                booking.BookingId, _paymentGateway.ProviderName, result.Status, result.FailureReason);
            throw new IllegalOperationException(result.FailureReason is null
                ? "Payment was not successful. Please try again."
                : $"Payment failed: {result.FailureReason}");
        }

        var payment = new Payment
        {
            BookingId = booking.BookingId,
            Amount = payable,
            PaymentMethod = paymentMethod,
            PaymentStatus = result.Status,
            TransactionRef = result.TransactionRef,
            Gateway = _paymentGateway.ProviderName,
            // Brand + last four only - the PAN and CVV are discarded with the request.
            CardSummary = card?.MaskedSummary,
            CreatedAt = DateTime.UtcNow,
            UpdatedAt = DateTime.UtcNow
        };
        var savedPayment = await _paymentRepository.AddAsync(payment, ct);

        booking.OrderNumber = GenerateOrderNumber(booking.BookingId);
        booking.BookingStatus = BookingStatus.CONFIRMED;

        var invoice = await _invoiceService.GenerateAsync(booking, savedPayment, ct);

        // Best-effort: a broken mail server shouldn't roll back a successful payment.
        try
        {
            var receiptData = await _invoiceService.BuildReceiptDataAsync(invoice.InvoiceId, ct);
            var pdf = _receiptPdfService.Generate(receiptData);
            await _receiptEmailService.SendReceiptEmailAsync(receiptData, pdf, ct);
        }
        catch (Exception ex)
        {
            _logger.LogWarning(ex, "Failed to send receipt e-mail for booking {BookingId}", booking.BookingId);
        }

        return ToDto(savedPayment, booking.OrderNumber);
    }

    public async Task<PaymentSummaryResponse> GetPaymentSummaryAsync(long bookingId, CancellationToken ct = default)
    {
        var customer = await _currentUser.CurrentCustomerAsync(ct);

        var booking = _bookingRepository.Query().FirstOrDefault(b => b.BookingId == bookingId)
            ?? throw new ResourceNotFoundException("Booking not found");
        if (booking.CustomerId != customer.CustomerId)
        {
            throw new ResourceNotFoundException("Booking not found");
        }

        var passengers = _passengerRepository.Query().Where(p => p.BookingId == bookingId).ToList();
        var addons = _bookingAddonRepository.Query().Where(a => a.BookingId == bookingId).ToList();

        var dto = new PaymentSummaryResponse
        {
            BookingId = booking.BookingId,
            BookingStatus = booking.BookingStatus.ToString(),
            OrderNumber = booking.OrderNumber,
            TourId = _bookingRepository.Query().Where(b => b.BookingId == bookingId).Select(b => b.Schedule.TourId).First(),
            TourTitle = _bookingRepository.Query().Where(b => b.BookingId == bookingId).Select(b => b.Schedule.Tour.Title).First(),
            DepartureDate = _bookingRepository.Query().Where(b => b.BookingId == bookingId).Select(b => b.Schedule.DepartureDate).First(),
            ReturnDate = _bookingRepository.Query().Where(b => b.BookingId == bookingId).Select(b => b.Schedule.ReturnDate).First(),
            NumberOfPassengers = booking.NumberOfPassengers,
            Passengers = passengers.Select(ToPassengerDto).ToList(),
            Addons = addons.Select(a => new BookingAddonResponse(a.BookingAddonId, a.AddonId, a.AddonName, a.PriceType.ToString(),
                a.UnitPrice, a.Quantity, a.TotalAddonCost)).ToList()
        };

        // Re-run the same pricing engine used at booking time so the room requirement and
        // per-band lines shown here match the stored total.
        if (passengers.Count != 0)
        {
            var inputs = passengers.Select(p => new PassengerInput
            {
                FullName = p.FullName,
                Gender = p.Gender,
                Dob = p.Dob,
                NeedsExtraBed = p.NeedsExtraBed,
                Occupancy = p.Occupancy
            }).ToList();

            var schedule = _bookingRepository.Query().Where(b => b.BookingId == bookingId).Select(b => b.Schedule).First();

            // Uses the charge STORED on each passenger, not today's admin price - so a later
            // price change never rewrites what an existing customer was quoted.
            var storedRoomCharges = new Dictionary<Occupancy, decimal>();
            foreach (var p in passengers)
            {
                if (p.Occupancy is { } occ && p.RoomCharge is { } charge)
                {
                    storedRoomCharges.TryAdd(occ, charge);
                }
            }

            var cost = _tourCostRepository.Query()
                .Where(c => c.TourId == schedule.TourId && c.Status == 1 && c.ValidFrom <= schedule.DepartureDate && c.ValidTo >= schedule.DepartureDate)
                .OrderByDescending(c => c.CostId)
                .FirstOrDefault();

            var pricing = _pricingCalculator.Calculate(inputs, schedule.DepartureDate, cost, schedule.Price, storedRoomCharges);
            dto.RoomSummary = pricing.RoomSummary;
            dto.Breakdown = pricing.Breakdown;
        }

        // booking.TotalAmount is the pre-tax subtotal; tax mirrors the same helpers ChargeAsync
        // uses, so what is displayed and what is charged cannot drift apart.
        var subTotal = booking.TotalAmount;
        var discount = 0m;
        var taxable = subTotal - discount;
        var tax = GstOn(taxable);

        dto.SubTotal = subTotal;
        dto.DiscountAmount = discount;
        dto.TaxAmount = tax;
        dto.GstRatePercent = _gstRate * 100;
        dto.GrandTotal = taxable + tax;
        dto.AlreadyPaid = _paymentRepository.Query().Any(p => p.BookingId == bookingId && p.PaymentStatus == PaymentStatus.SUCCESS);

        return dto;
    }

    public async Task<PaymentResponse> GetPaymentAsync(long paymentId, CancellationToken ct = default)
    {
        Payment payment;
        if (_currentUser.IsAdmin)
        {
            payment = await _paymentRepository.GetByIdAsync(paymentId, ct) ?? throw new ResourceNotFoundException("Payment not found");
        }
        else
        {
            var customer = await _currentUser.CurrentCustomerAsync(ct);
            payment = _paymentRepository.Query().FirstOrDefault(p => p.PaymentId == paymentId && p.Booking.CustomerId == customer.CustomerId)
                ?? throw new ResourceNotFoundException("Payment not found");
        }

        var orderNumber = _paymentRepository.Query().Where(p => p.PaymentId == paymentId).Select(p => p.Booking.OrderNumber).FirstOrDefault();
        return ToDto(payment, orderNumber);
    }

    private string CustomerEmailFor(Booking booking) =>
        _bookingRepository.Query().Where(b => b.BookingId == booking.BookingId).Select(b => b.Customer.Email).First();

    private static PassengerDto ToPassengerDto(Passenger p) => new()
    {
        PassengerId = p.PassengerId,
        BookingId = p.BookingId,
        FullName = p.FullName,
        Gender = p.Gender,
        Dob = p.Dob,
        Nationality = p.Nationality,
        IdProofType = p.IdProofType,
        IdProofNumber = p.IdProofNumber,
        NeedsExtraBed = p.NeedsExtraBed,
        PassengerType = p.PassengerType?.ToString(),
        Occupancy = p.Occupancy?.ToString(),
        RoomCharge = p.RoomCharge,
        PassengerPrice = p.PassengerPrice,
        AddressLine1 = p.AddressLine1,
        AddressLine2 = p.AddressLine2,
        City = p.City,
        State = p.State,
        Country = p.Country,
        Pincode = p.Pincode
    };

    private static string GenerateOrderNumber(long bookingId) => $"ORD-{bookingId}-{Guid.NewGuid().ToString("N")[..8].ToUpperInvariant()}";

    private static PaymentResponse ToDto(Payment p, string? orderNumber) => new(p.PaymentId, p.BookingId, p.Amount, p.PaymentMethod,
        p.PaymentStatus.ToString(), p.TransactionRef, p.CreatedAt)
    {
        Gateway = p.Gateway,
        CardSummary = p.CardSummary,
        UpdatedAt = p.UpdatedAt,
        OrderNumber = orderNumber
    };
}
