using eTour.Application.Dtos;

namespace eTour.Application.Services;

public interface ICartService
{
    Task<CartResponse> AddToCartAsync(CartRequest request, CancellationToken ct = default);
    Task<IReadOnlyList<CartResponse>> GetMyCartAsync(CancellationToken ct = default);
    Task RemoveFromCartAsync(long cartId, CancellationToken ct = default);
    Task<BookingResponse> CheckoutAsync(long cartId, CancellationToken ct = default);
}
