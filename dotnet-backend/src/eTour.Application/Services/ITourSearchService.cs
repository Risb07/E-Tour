using eTour.Application.Dtos;

namespace eTour.Application.Services;

public interface ITourSearchService
{
    Task<PagedResult<TourSearchResultDto>> SearchAsync(TourSearchRequest request, CancellationToken ct = default);
}
