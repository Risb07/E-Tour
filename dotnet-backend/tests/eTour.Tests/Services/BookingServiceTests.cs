using eTour.Application.Dtos;
using eTour.Application.Pricing;
using eTour.Application.Services;
using eTour.Domain.Entities;
using eTour.Domain.Enums;
using eTour.Domain.Exceptions;
using eTour.Infrastructure.Persistence;
using eTour.Tests.TestSupport;
using FluentAssertions;
using Microsoft.EntityFrameworkCore;
using NUnit.Framework;

namespace eTour.Tests.Services;

[TestFixture]
public class BookingServiceTests
{
    private EtourDbContext _context = null!;
    private BookingService _service = null!;
    private FakeCurrentUserService _currentUser = null!;
    private Customer _customer = null!;
    private TourSchedule _schedule = null!;
    private long _roleId;

    [SetUp]
    public async Task SetUp()
    {
        _context = TestDbContextFactory.Create();

        var role = new Role { RoleName = "CUSTOMER", Status = true };
        _context.Roles.Add(role);
        await _context.SaveChangesAsync();
        _roleId = role.RoleId;

        var user = new User { FirstName = "A", LastName = "B", Email = "a@b.com", PasswordHash = "x", Status = true, RoleId = role.RoleId };
        _context.Users.Add(user);
        await _context.SaveChangesAsync();

        _customer = new Customer { UserId = user.UserId, FullName = "Jane Doe", Email = "a@b.com" };
        _context.Customers.Add(_customer);

        var tour = new Tour { Title = "Test Tour", DurationDays = 5, BasePrice = 20000m, TourCode = TourCode.DOM, Status = TourStatus.ACTIVE };
        _context.Tours.Add(tour);
        await _context.SaveChangesAsync();

        _schedule = new TourSchedule { TourId = tour.TourId, DepartureDate = new DateOnly(2027, 6, 1), ReturnDate = new DateOnly(2027, 6, 6), AvailableSeats = 2, Price = 20000m };
        _context.TourSchedules.Add(_schedule);
        await _context.SaveChangesAsync();

        _currentUser = new FakeCurrentUserService { CurrentCustomerEntity = _customer };

        _service = new BookingService(
            new FakeBookingLockRepository(_context),
            new FakeTourScheduleLockRepository(_context),
            new GenericRepository<Booking, long>(_context),
            new GenericRepository<TourSchedule, long>(_context),
            new GenericRepository<Tour, long>(_context),
            new GenericRepository<TourAddon, long>(_context),
            new GenericRepository<BookingAddon, long>(_context),
            new GenericRepository<Passenger, long>(_context),
            new GenericRepository<TourCost, long>(_context),
            new GenericRepository<RoomCharge, long>(_context),
            new TourPricingCalculator(),
            _currentUser);
    }

    [TearDown]
    public void TearDown() => _context.Dispose();

    [Test]
    public async Task CreateBooking_WithoutPassengers_UsesFlatEstimate_AndHoldsSeats()
    {
        var request = new BookingRequest { ScheduleId = _schedule.ScheduleId, NumberOfPassengers = 2 };

        var response = await _service.CreateBookingAsync(request);

        response.TotalAmount.Should().Be(40000m); // 2 * flat schedule price
        response.PassengersFinalized.Should().BeFalse();

        var updatedSchedule = await _context.TourSchedules.FindAsync(_schedule.ScheduleId);
        updatedSchedule!.AvailableSeats.Should().Be(0);
    }

    [Test]
    public async Task CreateBooking_NotEnoughSeats_ThrowsIllegalOperationException()
    {
        var request = new BookingRequest { ScheduleId = _schedule.ScheduleId, NumberOfPassengers = 5 };

        var act = async () => await _service.CreateBookingAsync(request);

        await act.Should().ThrowAsync<IllegalOperationException>().WithMessage("*Only 2 seat*");
    }

    [Test]
    public async Task CreateBooking_DeclaredCompositionWithNoAdult_Throws()
    {
        var request = new BookingRequest { ScheduleId = _schedule.ScheduleId, NumberOfPassengers = 1, AdultCount = 0, ChildCount = 1 };

        var act = async () => await _service.CreateBookingAsync(request);

        await act.Should().ThrowAsync<IllegalOperationException>().WithMessage("*At least one adult*");
    }

    [Test]
    public async Task CreateBooking_AdultSlotWithChildDob_Throws()
    {
        // Declares 1 adult, but the passenger's DOB makes them a child on the departure date.
        var request = new BookingRequest
        {
            ScheduleId = _schedule.ScheduleId,
            NumberOfPassengers = 1,
            AdultCount = 1,
            ChildCount = 0,
            Passengers = [new PassengerInput { FullName = "Kid", Dob = new DateOnly(2020, 1, 1) }]
        };

        var act = async () => await _service.CreateBookingAsync(request);

        await act.Should().ThrowAsync<IllegalOperationException>().WithMessage("*booked as an adult*");
    }

    [Test]
    public async Task CreateBooking_WithValidAdultPassenger_PricesAndPersistsPassenger()
    {
        var request = new BookingRequest
        {
            ScheduleId = _schedule.ScheduleId,
            NumberOfPassengers = 1,
            AdultCount = 1,
            Passengers = [new PassengerInput { FullName = "Jane Doe", Dob = new DateOnly(1990, 1, 1) }]
        };

        var response = await _service.CreateBookingAsync(request);

        response.PassengersFinalized.Should().BeTrue();
        response.TotalAmount.Should().BeGreaterThan(0);
        (await _context.Passengers.CountAsync(p => p.BookingId == response.BookingId)).Should().Be(1);
    }

    [Test]
    public async Task GetBookingById_NotOwnedByCurrentCustomer_Returns404NotForbidden()
    {
        var request = new BookingRequest { ScheduleId = _schedule.ScheduleId, NumberOfPassengers = 1, AdultCount = 1 };
        var created = await _service.CreateBookingAsync(request);

        // Switch to a different, non-owning customer.
        var otherUser = new User { FirstName = "Other", LastName = "Guy", Email = "other@b.com", PasswordHash = "x", Status = true, RoleId = _roleId };
        _context.Users.Add(otherUser);
        await _context.SaveChangesAsync();
        var otherCustomer = new Customer { UserId = otherUser.UserId, FullName = "Other Guy", Email = "other@b.com" };
        _context.Customers.Add(otherCustomer);
        await _context.SaveChangesAsync();
        _currentUser.CurrentCustomerEntity = otherCustomer;

        var act = async () => await _service.GetBookingByIdAsync(created.BookingId);

        // 404, not 403/AccessDenied - ownership failures never confirm the booking exists.
        await act.Should().ThrowAsync<ResourceNotFoundException>();
    }

    [Test]
    public async Task CancelBooking_ReleasesSeatsBackToSchedule()
    {
        var request = new BookingRequest { ScheduleId = _schedule.ScheduleId, NumberOfPassengers = 2, AdultCount = 2 };
        var created = await _service.CreateBookingAsync(request);
        (await _context.TourSchedules.FindAsync(_schedule.ScheduleId))!.AvailableSeats.Should().Be(0);

        await _service.CancelBookingAsync(created.BookingId);

        (await _context.TourSchedules.FindAsync(_schedule.ScheduleId))!.AvailableSeats.Should().Be(2);
        var cancelled = await _context.Bookings.FindAsync(created.BookingId);
        cancelled!.BookingStatus.Should().Be(BookingStatus.CANCELLED);
    }

    [Test]
    public async Task CancelBooking_AlreadyCancelled_Throws()
    {
        var request = new BookingRequest { ScheduleId = _schedule.ScheduleId, NumberOfPassengers = 1, AdultCount = 1 };
        var created = await _service.CreateBookingAsync(request);
        await _service.CancelBookingAsync(created.BookingId);

        var act = async () => await _service.CancelBookingAsync(created.BookingId);

        await act.Should().ThrowAsync<IllegalOperationException>().WithMessage("*already cancelled*");
    }
}
