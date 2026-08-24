using eTour.Application.Dtos;
using eTour.Application.Gateways;
using eTour.Application.Pricing;
using eTour.Application.Services;
using eTour.Domain.Entities;
using eTour.Domain.Enums;
using eTour.Domain.Exceptions;
using eTour.Infrastructure.Persistence;
using eTour.Tests.TestSupport;
using FluentAssertions;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Logging.Abstractions;
using NUnit.Framework;

namespace eTour.Tests.Services;

[TestFixture]
public class PaymentServiceTests
{
    private EtourDbContext _context = null!;
    private PaymentService _service = null!;
    private FakeCurrentUserService _currentUser = null!;
    private Customer _customer = null!;
    private Booking _booking = null!;
    private FakeReceiptEmailService _receiptEmail = null!;

    [SetUp]
    public async Task SetUp()
    {
        _context = TestDbContextFactory.Create();

        var role = new Role { RoleName = "CUSTOMER", Status = true };
        _context.Roles.Add(role);
        await _context.SaveChangesAsync();

        var user = new User { FirstName = "A", LastName = "B", Email = "a@b.com", PasswordHash = "x", Status = true, RoleId = role.RoleId };
        _context.Users.Add(user);
        await _context.SaveChangesAsync();

        _customer = new Customer { UserId = user.UserId, FullName = "Jane Doe", Email = "jane@example.com" };
        _context.Customers.Add(_customer);

        var tour = new Tour { Title = "Test Tour", DurationDays = 5, BasePrice = 20000m, TourCode = TourCode.DOM, Status = TourStatus.ACTIVE };
        _context.Tours.Add(tour);
        await _context.SaveChangesAsync();

        var schedule = new TourSchedule { TourId = tour.TourId, DepartureDate = new DateOnly(2027, 6, 1), ReturnDate = new DateOnly(2027, 6, 6), AvailableSeats = 10, Price = 20000m };
        _context.TourSchedules.Add(schedule);
        await _context.SaveChangesAsync();

        _booking = new Booking
        {
            CustomerId = _customer.CustomerId,
            ScheduleId = schedule.ScheduleId,
            BookingDate = DateOnly.FromDateTime(DateTime.UtcNow),
            BookingStatus = BookingStatus.PENDING,
            NumberOfPassengers = 1,
            AdultCount = 1,
            TotalAmount = 20000m
        };
        _context.Bookings.Add(_booking);
        await _context.SaveChangesAsync();
        // A finalized booking needs exactly NumberOfPassengers passenger rows before it's payable.
        _context.Passengers.Add(new Passenger { BookingId = _booking.BookingId, FullName = "Jane Doe", Dob = new DateOnly(1990, 1, 1) });
        await _context.SaveChangesAsync();

        _currentUser = new FakeCurrentUserService { CurrentCustomerEntity = _customer };
        _receiptEmail = new FakeReceiptEmailService();

        var configuration = new ConfigurationBuilder()
            .AddInMemoryCollection(new Dictionary<string, string?> { ["Invoice:GstRate"] = "0.05" })
            .Build();

        var invoiceService = new InvoiceService(
            new GenericRepository<Invoice, long>(_context),
            new GenericRepository<Passenger, long>(_context),
            new GenericRepository<Booking, long>(_context),
            new FakeReceiptPdfService(),
            _currentUser,
            configuration);

        _service = new PaymentService(
            new FakeBookingLockRepository(_context),
            new GenericRepository<Booking, long>(_context),
            new GenericRepository<Payment, long>(_context),
            new GenericRepository<Passenger, long>(_context),
            new GenericRepository<BookingAddon, long>(_context),
            new GenericRepository<TourCost, long>(_context),
            _currentUser,
            invoiceService,
            new FakeReceiptPdfService(),
            _receiptEmail,
            new SimulatedPaymentGateway(NullLogger<SimulatedPaymentGateway>.Instance),
            new TourPricingCalculator(),
            configuration,
            NullLogger<PaymentService>.Instance);
    }

    [TearDown]
    public void TearDown() => _context.Dispose();

    [Test]
    public async Task RecordPayment_ChargesSubtotalPlusGst_AndConfirmsBooking()
    {
        var response = await _service.RecordPaymentAsync(new PaymentRequest { BookingId = _booking.BookingId, PaymentMethod = "CASH" });

        response.Amount.Should().Be(21000m); // 20000 + 5% GST
        response.PaymentStatus.Should().Be("SUCCESS");
        response.OrderNumber.Should().NotBeNullOrEmpty();

        var updatedBooking = await _context.Bookings.FindAsync(_booking.BookingId);
        updatedBooking!.BookingStatus.Should().Be(BookingStatus.CONFIRMED);
    }

    [Test]
    public async Task RecordPayment_AlreadyConfirmedBooking_Throws()
    {
        await _service.RecordPaymentAsync(new PaymentRequest { BookingId = _booking.BookingId, PaymentMethod = "CASH" });

        var act = async () => await _service.RecordPaymentAsync(new PaymentRequest { BookingId = _booking.BookingId, PaymentMethod = "CASH" });

        await act.Should().ThrowAsync<IllegalOperationException>().WithMessage("*already paid*");
    }

    [Test]
    public async Task RecordPayment_CancelledBooking_Throws()
    {
        _booking.BookingStatus = BookingStatus.CANCELLED;
        await _context.SaveChangesAsync();

        var act = async () => await _service.RecordPaymentAsync(new PaymentRequest { BookingId = _booking.BookingId, PaymentMethod = "CASH" });

        await act.Should().ThrowAsync<IllegalOperationException>().WithMessage("*cancelled*");
    }

    [Test]
    public async Task RecordPayment_IncompletePassengerDetails_Throws()
    {
        _booking.NumberOfPassengers = 2; // only 1 passenger row exists
        await _context.SaveChangesAsync();

        var act = async () => await _service.RecordPaymentAsync(new PaymentRequest { BookingId = _booking.BookingId, PaymentMethod = "CASH" });

        await act.Should().ThrowAsync<IllegalOperationException>().WithMessage("*incomplete*");
    }

    [Test]
    public async Task PayWithCard_InvalidLuhnNumber_ThrowsBeforeReachingGateway()
    {
        var request = new CardPaymentRequest
        {
            BookingId = _booking.BookingId,
            CardNumber = "1234567890123456",
            CardHolderName = "Jane Doe",
            ExpiryMonth = 12,
            ExpiryYear = 2030,
            Cvv = "123"
        };

        var act = async () => await _service.PayWithCardAsync(request);

        await act.Should().ThrowAsync<IllegalOperationException>().WithMessage("*card number is not valid*");
    }

    [Test]
    public async Task PayWithCard_ValidCard_SucceedsAndMasksCardSummary()
    {
        var request = new CardPaymentRequest
        {
            BookingId = _booking.BookingId,
            CardNumber = "4242424242424242",
            CardHolderName = "Jane Doe",
            ExpiryMonth = 12,
            ExpiryYear = 2030,
            Cvv = "123"
        };

        var response = await _service.PayWithCardAsync(request);

        response.CardSummary.Should().Be("VISA ****4242");
        response.Amount.Should().Be(21000m);
    }

    [Test]
    public async Task PayWithCard_DeclineTestCard_ThrowsWithoutConfirmingBooking()
    {
        var request = new CardPaymentRequest
        {
            BookingId = _booking.BookingId,
            CardNumber = "4000000000000002", // simulated decline prefix
            CardHolderName = "Jane Doe",
            ExpiryMonth = 12,
            ExpiryYear = 2030,
            Cvv = "123"
        };

        var act = async () => await _service.PayWithCardAsync(request);
        await act.Should().ThrowAsync<IllegalOperationException>().WithMessage("*declined*");

        (await _context.Bookings.FindAsync(_booking.BookingId))!.BookingStatus.Should().Be(BookingStatus.PENDING);
    }

    [Test]
    public async Task GetPaymentSummary_ComputesGrandTotalConsistentlyWithCharge()
    {
        var summary = await _service.GetPaymentSummaryAsync(_booking.BookingId);

        summary.SubTotal.Should().Be(20000m);
        summary.TaxAmount.Should().Be(1000m);
        summary.GrandTotal.Should().Be(21000m);
        summary.AlreadyPaid.Should().BeFalse();
    }

    private class FakeReceiptPdfService : IReceiptPdfService
    {
        public byte[] Generate(ReceiptData data) => [1, 2, 3];
    }

    private class FakeReceiptEmailService : IReceiptEmailService
    {
        public bool WasCalled { get; private set; }
        public Task SendReceiptEmailAsync(ReceiptData data, byte[] pdf, CancellationToken ct = default)
        {
            WasCalled = true;
            return Task.CompletedTask;
        }
    }
}
