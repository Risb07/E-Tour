using eTour.Application.Common;
using eTour.Application.Dtos;
using eTour.Domain.Entities;
using eTour.Domain.Enums;
using eTour.Domain.Exceptions;
using Microsoft.Extensions.Configuration;

namespace eTour.Application.Services;

public class InvoiceService : IInvoiceService
{
    private readonly decimal _gstRate;
    private readonly IGenericRepository<Invoice, long> _invoiceRepository;
    private readonly IGenericRepository<Passenger, long> _passengerRepository;
    private readonly IGenericRepository<Booking, long> _bookingRepository;
    private readonly IReceiptPdfService _receiptPdfService;
    private readonly ICurrentUserService _currentUser;

    public InvoiceService(IGenericRepository<Invoice, long> invoiceRepository, IGenericRepository<Passenger, long> passengerRepository,
        IGenericRepository<Booking, long> bookingRepository, IReceiptPdfService receiptPdfService, ICurrentUserService currentUser,
        IConfiguration configuration)
    {
        _invoiceRepository = invoiceRepository;
        _passengerRepository = passengerRepository;
        _bookingRepository = bookingRepository;
        _receiptPdfService = receiptPdfService;
        _currentUser = currentUser;
        _gstRate = configuration.GetValue<decimal?>("Invoice:GstRate") ?? 0.05m;
    }

    public async Task<Invoice> GenerateAsync(Booking booking, Payment payment, CancellationToken ct = default)
    {
        var subTotal = booking.TotalAmount;
        var tax = Math.Round(subTotal * _gstRate, 2, MidpointRounding.AwayFromZero);
        var total = subTotal + tax;

        var invoice = new Invoice
        {
            InvoiceNumber = $"INV-{booking.BookingId}-{DateTimeOffset.UtcNow.ToUnixTimeMilliseconds()}",
            BookingId = booking.BookingId,
            PaymentId = payment.PaymentId,
            CustomerId = booking.CustomerId,
            InvoiceDate = DateOnly.FromDateTime(DateTime.UtcNow),
            SubTotal = subTotal,
            TaxAmount = tax,
            DiscountAmount = 0m,
            TotalAmount = total,
            InvoiceStatus = InvoiceStatus.GENERATED,
            CreatedAt = DateTime.UtcNow
        };

        return await _invoiceRepository.AddAsync(invoice, ct);
    }

    public async Task<InvoiceResponse> GetByBookingAsync(long bookingId, CancellationToken ct = default)
    {
        var invoice = await RequireForBookingAsync(bookingId, ct);
        return ToDto(invoice);
    }

    public async Task<IReadOnlyList<InvoiceResponse>> GetMyInvoicesAsync(CancellationToken ct = default)
    {
        var customer = await _currentUser.CurrentCustomerAsync(ct);
        var invoices = _invoiceRepository.Query().Where(i => i.CustomerId == customer.CustomerId).ToList();
        return invoices.Select(ToDto).ToList();
    }

    public async Task<byte[]> GetReceiptPdfAsync(long bookingId, CancellationToken ct = default)
    {
        var invoice = await RequireForBookingAsync(bookingId, ct);
        var data = await BuildReceiptDataAsync(invoice.InvoiceId, ct);
        return _receiptPdfService.Generate(data);
    }

    public Task<ReceiptData> BuildReceiptDataAsync(long invoiceId, CancellationToken ct = default)
    {
        var invoice = _invoiceRepository.Query().FirstOrDefault(i => i.InvoiceId == invoiceId)
            ?? throw new ResourceNotFoundException("Invoice not found");

        // Projected (not Include-loaded) so this works regardless of what the generic
        // repository happens to have eager-loaded.
        var projected = _bookingRepository.Query().Where(b => b.BookingId == invoice.BookingId)
            .Select(b => new
            {
                CustomerFullName = b.Customer.FullName,
                CustomerEmail = b.Customer.Email,
                TourTitle = b.Schedule.Tour.Title,
                b.Schedule.DepartureDate,
                b.Schedule.ReturnDate,
                b.NumberOfPassengers
            })
            .First();

        var passengers = _passengerRepository.Query().Where(p => p.BookingId == invoice.BookingId)
            .Select(p => new ReceiptPassengerLine
            {
                FullName = p.FullName,
                Gender = p.Gender,
                IdProofNumber = p.IdProofNumber,
                AddressLine1 = p.AddressLine1,
                AddressLine2 = p.AddressLine2,
                City = p.City,
                State = p.State,
                Country = p.Country,
                Pincode = p.Pincode
            }).ToList();

        return Task.FromResult(new ReceiptData
        {
            InvoiceNumber = invoice.InvoiceNumber,
            InvoiceDate = invoice.InvoiceDate,
            OrderNumber = _bookingRepository.Query().Where(b => b.BookingId == invoice.BookingId).Select(b => b.OrderNumber).FirstOrDefault(),
            CustomerFullName = projected.CustomerFullName,
            CustomerEmail = projected.CustomerEmail,
            TourTitle = projected.TourTitle,
            DepartureDate = projected.DepartureDate,
            ReturnDate = projected.ReturnDate,
            NumberOfPassengers = projected.NumberOfPassengers,
            SubTotal = invoice.SubTotal,
            TaxAmount = invoice.TaxAmount,
            DiscountAmount = invoice.DiscountAmount,
            TotalAmount = invoice.TotalAmount,
            Passengers = passengers
        });
    }

    private async Task<Invoice> RequireForBookingAsync(long bookingId, CancellationToken ct)
    {
        var invoice = _invoiceRepository.Query().FirstOrDefault(i => i.BookingId == bookingId)
            ?? throw new ResourceNotFoundException("No invoice for this booking");

        if (!_currentUser.IsAdmin)
        {
            var customer = await _currentUser.CurrentCustomerAsync(ct);
            if (invoice.CustomerId != customer.CustomerId)
            {
                throw new ResourceNotFoundException("No invoice for this booking");
            }
        }
        return invoice;
    }

    private static InvoiceResponse ToDto(Invoice i) => new()
    {
        InvoiceId = i.InvoiceId,
        InvoiceNumber = i.InvoiceNumber,
        BookingId = i.BookingId,
        PaymentId = i.PaymentId,
        CustomerId = i.CustomerId,
        InvoiceDate = i.InvoiceDate,
        SubTotal = i.SubTotal,
        TaxAmount = i.TaxAmount,
        DiscountAmount = i.DiscountAmount,
        TotalAmount = i.TotalAmount,
        InvoiceStatus = i.InvoiceStatus.ToString()
    };
}
