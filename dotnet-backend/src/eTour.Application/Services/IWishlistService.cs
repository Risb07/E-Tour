using eTour.Application.Dtos;

namespace eTour.Application.Services;

public interface IWishlistService
{
    Task<IReadOnlyList<WishlistItemResponse>> GetMyWishlistAsync(CancellationToken ct = default);
    Task<bool> IsSavedAsync(long tourId, CancellationToken ct = default);
    Task<WishlistItemResponse> AddAsync(long tourId, CancellationToken ct = default);
    Task RemoveAsync(long tourId, CancellationToken ct = default);
}
