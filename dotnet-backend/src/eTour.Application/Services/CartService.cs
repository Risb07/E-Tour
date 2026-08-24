using eTour.Application.Common;
using eTour.Application.Dtos;
using eTour.Domain.Entities;
using eTour.Domain.Enums;
using eTour.Domain.Exceptions;

namespace eTour.Application.Services;

public class CartService : ICartService
{
    private readonly IGenericRepository<Cart, long> _cartRepository;
    private readonly IGenericRepository<CartAddon, long> _cartAddonRepository;
    private readonly IGenericRepository<TourSchedule, long> _scheduleRepository;
    private readonly IGenericRepository<TourAddon, long> _addonRepository;
    private readonly IGenericRepository<Tour, long> _tourRepository;
    private readonly ICurrentUserService _currentUser;
    private readonly IBookingService _bookingService;

    public CartService(IGenericRepository<Cart, long> cartRepository, IGenericRepository<CartAddon, long> cartAddonRepository,
        IGenericRepository<TourSchedule, long> scheduleRepository, IGenericRepository<TourAddon, long> addonRepository,
        IGenericRepository<Tour, long> tourRepository, ICurrentUserService currentUser, IBookingService bookingService)
    {
        _cartRepository = cartRepository;
        _cartAddonRepository = cartAddonRepository;
        _scheduleRepository = scheduleRepository;
        _addonRepository = addonRepository;
        _tourRepository = tourRepository;
        _currentUser = currentUser;
        _bookingService = bookingService;
    }

    public async Task<CartResponse> AddToCartAsync(CartRequest request, CancellationToken ct = default)
    {
        var customer = await _currentUser.CurrentCustomerAsync(ct);

        var schedule = await _scheduleRepository.GetByIdAsync(request.ScheduleId, ct)
            ?? throw new ResourceNotFoundException("Tour schedule not found");

        var numberOfPassengers = request.AdultCount + request.ChildCount;

        var cart = new Cart
        {
            CustomerId = customer.CustomerId,
            ScheduleId = schedule.ScheduleId,
            PaxSummary = $"{numberOfPassengers} passenger(s)",
            Status = CartStatus.ACTIVE,
            AdultCount = request.AdultCount,
            ChildCount = request.ChildCount,
            CreatedAt = DateTime.UtcNow,
            UpdatedAt = DateTime.UtcNow
        };

        var estimated = schedule.Price * numberOfPassengers;
        var savedCart = await _cartRepository.AddAsync(cart, ct);

        var addonRows = new List<CartAddon>();
        foreach (var sel in request.Addons)
        {
            // Scoped to this schedule's tour and to active add-ons only - the same rule the
            // booking path applies, so a cart can't carry an add-on checkout would reject.
            var catalog = _addonRepository.Query()
                .FirstOrDefault(a => a.AddonId == sel.AddonId && a.TourId == schedule.TourId && a.Status)
                ?? throw new ResourceNotFoundException($"Add-on not found: {sel.AddonId}");

            var lineTotal = catalog.Price * sel.Quantity;
            var addonRow = new CartAddon { CartId = savedCart.CartId, AddonId = catalog.AddonId, Quantity = sel.Quantity, EstimatedCost = lineTotal };
            estimated += lineTotal;
            addonRows.Add(addonRow);
        }
        foreach (var row in addonRows)
        {
            await _cartAddonRepository.AddAsync(row, ct);
        }

        savedCart.EstimatedAmount = estimated;
        savedCart.UpdatedAt = DateTime.UtcNow;
        await _cartRepository.UpdateAsync(savedCart, ct);

        return ToResponse(savedCart, schedule, addonRows);
    }

    public async Task<IReadOnlyList<CartResponse>> GetMyCartAsync(CancellationToken ct = default)
    {
        var customer = await _currentUser.CurrentCustomerAsync(ct);
        var carts = _cartRepository.Query().Where(c => c.CustomerId == customer.CustomerId && c.Status == CartStatus.ACTIVE).ToList();

        return carts.Select(cart =>
        {
            var schedule = _scheduleRepository.Query().First(s => s.ScheduleId == cart.ScheduleId);
            var addons = _cartAddonRepository.Query().Where(a => a.CartId == cart.CartId).ToList();
            return ToResponse(cart, schedule, addons);
        }).ToList();
    }

    public async Task RemoveFromCartAsync(long cartId, CancellationToken ct = default)
    {
        var customer = await _currentUser.CurrentCustomerAsync(ct);
        var cart = _cartRepository.Query().FirstOrDefault(c => c.CartId == cartId && c.CustomerId == customer.CustomerId)
            ?? throw new ResourceNotFoundException("Cart item not found");

        var addons = _cartAddonRepository.Query().Where(a => a.CartId == cartId).ToList();
        foreach (var addon in addons)
        {
            await _cartAddonRepository.DeleteAsync(addon, ct);
        }
        await _cartRepository.DeleteAsync(cart, ct);
    }

    public async Task<BookingResponse> CheckoutAsync(long cartId, CancellationToken ct = default)
    {
        var customer = await _currentUser.CurrentCustomerAsync(ct);
        var cart = _cartRepository.Query().FirstOrDefault(c => c.CartId == cartId && c.CustomerId == customer.CustomerId)
            ?? throw new ResourceNotFoundException("Cart item not found");

        if (cart.Status != CartStatus.ACTIVE)
        {
            throw new IllegalOperationException("This cart item is no longer active");
        }

        var cartAddons = _cartAddonRepository.Query().Where(a => a.CartId == cartId).ToList();

        var bookingRequest = new BookingRequest
        {
            ScheduleId = cart.ScheduleId,
            // pax_summary was stored as "<n> passenger(s)" - recover the number.
            NumberOfPassengers = ExtractPassengerCount(cart.PaxSummary),
            AdultCount = cart.AdultCount,
            ChildCount = cart.ChildCount,
            Addons = cartAddons.Select(ca => new BookingAddonSelection { AddonId = ca.AddonId, Quantity = ca.Quantity }).ToList()
        };

        var booking = await _bookingService.CreateBookingAsync(bookingRequest, ct);

        cart.Status = CartStatus.CONVERTED_TO_BOOKING;
        cart.UpdatedAt = DateTime.UtcNow;
        await _cartRepository.UpdateAsync(cart, ct);

        return booking;
    }

    private static int ExtractPassengerCount(string? paxSummary)
    {
        if (paxSummary is null)
        {
            return 1;
        }
        var firstToken = paxSummary.Trim().Split(' ')[0];
        return int.TryParse(firstToken, out var count) ? count : 1;
    }

    private CartResponse ToResponse(Cart cart, TourSchedule schedule, List<CartAddon> addons)
    {
        var tourTitle = _tourRepository.Query().Where(t => t.TourId == schedule.TourId).Select(t => t.Title).FirstOrDefault() ?? "";
        var addonDtos = addons.Select(a =>
        {
            var addonName = _addonRepository.Query().Where(x => x.AddonId == a.AddonId).Select(x => x.AddonName).FirstOrDefault() ?? "";
            return new CartAddonResponse { CartAddonId = a.CartAddonId, AddonId = a.AddonId, AddonName = addonName, Quantity = a.Quantity, EstimatedCost = a.EstimatedCost };
        }).ToList();

        return new CartResponse
        {
            CartId = cart.CartId,
            ScheduleId = schedule.ScheduleId,
            TourId = schedule.TourId,
            TourTitle = tourTitle,
            DepartureDate = schedule.DepartureDate,
            PaxSummary = cart.PaxSummary,
            AdultCount = cart.AdultCount,
            ChildCount = cart.ChildCount,
            EstimatedAmount = cart.EstimatedAmount,
            Status = cart.Status.ToString(),
            CreatedAt = cart.CreatedAt,
            Addons = addonDtos
        };
    }
}
