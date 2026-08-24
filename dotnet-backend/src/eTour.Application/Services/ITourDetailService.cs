using eTour.Application.Dtos;

namespace eTour.Application.Services;

/// <summary>Covers every "Tour Page" tab from the BRD (Journey, Stay & Meals, content tabs, media, add-ons, itinerary).</summary>
public interface ITourDetailService
{
    Task<IReadOnlyList<JourneyDetailDto>> GetJourneyAsync(long tourId, CancellationToken ct = default);
    Task<JourneyDetailDto> AddJourneyLegAsync(long tourId, JourneyDetailDto dto, CancellationToken ct = default);

    Task<IReadOnlyList<StayMealDto>> GetStayMealsAsync(long tourId, CancellationToken ct = default);
    Task<StayMealDto> AddStayMealAsync(long tourId, StayMealDto dto, CancellationToken ct = default);
    Task DeleteStayMealAsync(long tourId, long stayMealId, CancellationToken ct = default);

    Task<IReadOnlyList<TourContentDto>> GetContentAsync(long tourId, CancellationToken ct = default);
    Task<TourContentDto> UpsertContentAsync(long tourId, TourContentDto dto, CancellationToken ct = default);
    Task DeleteContentAsync(long tourId, long tourContentId, CancellationToken ct = default);

    Task<IReadOnlyList<TourMediaDto>> GetMediaAsync(long tourId, CancellationToken ct = default);
    Task<TourMediaDto> AddMediaAsync(long tourId, TourMediaDto dto, CancellationToken ct = default);
    Task DeleteMediaAsync(long tourId, long mediaId, CancellationToken ct = default);

    Task<IReadOnlyList<TourAddonDto>> GetAddonsAsync(long tourId, CancellationToken ct = default);
    Task<TourAddonDto> AddAddonAsync(long tourId, TourAddonDto dto, CancellationToken ct = default);
    Task<TourAddonDto> UpdateAddonAsync(long tourId, long addonId, TourAddonDto dto, CancellationToken ct = default);
    Task DeleteAddonAsync(long tourId, long addonId, CancellationToken ct = default);

    Task<IReadOnlyList<ItineraryDto>> GetItineraryAsync(long tourId, CancellationToken ct = default);
    Task<ItineraryDto> AddItineraryDayAsync(long tourId, ItineraryDto dto, CancellationToken ct = default);
    Task<ItineraryDto> UpdateItineraryDayAsync(long tourId, long itineraryId, ItineraryDto dto, CancellationToken ct = default);
    Task DeleteItineraryDayAsync(long tourId, long itineraryId, CancellationToken ct = default);
}
