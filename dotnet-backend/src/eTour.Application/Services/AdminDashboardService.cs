using eTour.Application.Common;
using eTour.Application.Dtos;
using eTour.Domain.Entities;
using eTour.Domain.Enums;

namespace eTour.Application.Services;

public class AdminDashboardService : IAdminDashboardService
{
    private readonly IGenericRepository<User, long> _userRepository;
    private readonly IGenericRepository<Customer, long> _customerRepository;
    private readonly IGenericRepository<Tour, long> _tourRepository;
    private readonly IGenericRepository<Booking, long> _bookingRepository;
    private readonly IGenericRepository<Invoice, long> _invoiceRepository;

    public AdminDashboardService(IGenericRepository<User, long> userRepository, IGenericRepository<Customer, long> customerRepository,
        IGenericRepository<Tour, long> tourRepository, IGenericRepository<Booking, long> bookingRepository,
        IGenericRepository<Invoice, long> invoiceRepository)
    {
        _userRepository = userRepository;
        _customerRepository = customerRepository;
        _tourRepository = tourRepository;
        _bookingRepository = bookingRepository;
        _invoiceRepository = invoiceRepository;
    }

    public Task<DashboardStatsResponse> GetStatsAsync(CancellationToken ct = default)
    {
        var stats = new DashboardStatsResponse
        {
            TotalUsers = _userRepository.Query().LongCount(),
            TotalCustomers = _customerRepository.Query().LongCount(),
            TotalTours = _tourRepository.Query().LongCount(),
            ActiveTours = _tourRepository.Query().LongCount(t => t.Status == TourStatus.ACTIVE),
            TotalBookings = _bookingRepository.Query().LongCount(),
            ConfirmedBookings = _bookingRepository.Query().LongCount(b => b.BookingStatus == BookingStatus.CONFIRMED),
            PendingBookings = _bookingRepository.Query().LongCount(b => b.BookingStatus == BookingStatus.PENDING),
            CancelledBookings = _bookingRepository.Query().LongCount(b => b.BookingStatus == BookingStatus.CANCELLED),
            TotalRevenue = _invoiceRepository.Query().Sum(i => (decimal?)i.TotalAmount) ?? 0m
        };

        return Task.FromResult(stats);
    }
}
