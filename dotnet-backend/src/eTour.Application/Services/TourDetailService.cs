using AutoMapper;
using AutoMapper.QueryableExtensions;
using eTour.Application.Common;
using eTour.Application.Dtos;
using eTour.Domain.Entities;
using eTour.Domain.Exceptions;

namespace eTour.Application.Services;

public class TourDetailService : ITourDetailService
{
    private readonly IGenericRepository<Tour, long> _tourRepository;
    private readonly IGenericRepository<JourneyDetail, long> _journeyRepository;
    private readonly IGenericRepository<StayMeal, long> _stayMealRepository;
    private readonly IGenericRepository<TourContent, long> _contentRepository;
    private readonly IGenericRepository<TourMedia, long> _mediaRepository;
    private readonly IGenericRepository<TourAddon, long> _addonRepository;
    private readonly IGenericRepository<Itinerary, long> _itineraryRepository;
    private readonly IGenericRepository<Location, long> _locationRepository;
    private readonly IMapper _mapper;

    public TourDetailService(IGenericRepository<Tour, long> tourRepository, IGenericRepository<JourneyDetail, long> journeyRepository,
        IGenericRepository<StayMeal, long> stayMealRepository, IGenericRepository<TourContent, long> contentRepository,
        IGenericRepository<TourMedia, long> mediaRepository, IGenericRepository<TourAddon, long> addonRepository,
        IGenericRepository<Itinerary, long> itineraryRepository, IGenericRepository<Location, long> locationRepository,
        IMapper mapper)
    {
        _tourRepository = tourRepository;
        _journeyRepository = journeyRepository;
        _stayMealRepository = stayMealRepository;
        _contentRepository = contentRepository;
        _mediaRepository = mediaRepository;
        _addonRepository = addonRepository;
        _itineraryRepository = itineraryRepository;
        _locationRepository = locationRepository;
        _mapper = mapper;
    }

    private async Task RequireTourAsync(long tourId, CancellationToken ct) =>
        _ = await _tourRepository.GetByIdAsync(tourId, ct) ?? throw new ResourceNotFoundException($"Tour {tourId} was not found");

    private async Task<Location> RequireLocationAsync(long locationId, string which, CancellationToken ct) =>
        await _locationRepository.GetByIdAsync(locationId, ct)
        ?? throw new ResourceNotFoundException($"{which} location not found");

    // ----- Journey -----

    public Task<IReadOnlyList<JourneyDetailDto>> GetJourneyAsync(long tourId, CancellationToken ct = default)
    {
        // ProjectTo, not Map: the leg's From/ToLocationName come from navigations that Query()
        // never eager-loads, so an in-memory map would leave both null.
        var items = _journeyRepository.Query().Where(j => j.TourId == tourId).OrderBy(j => j.SequenceNo)
            .ProjectTo<JourneyDetailDto>(_mapper.ConfigurationProvider).ToList();
        return Task.FromResult<IReadOnlyList<JourneyDetailDto>>(items);
    }

    public async Task<JourneyDetailDto> AddJourneyLegAsync(long tourId, JourneyDetailDto dto, CancellationToken ct = default)
    {
        await RequireTourAsync(tourId, ct);
        var entity = _mapper.Map<JourneyDetail>(dto);
        entity.TourId = tourId;
        // Resolving the endpoints up front does two jobs, both matching Java: a bad id fails as a
        // clean 404 instead of an FK violation, and attaching the entities means the response
        // carries the resolved location names rather than nulls.
        entity.FromLocation = await RequireLocationAsync(dto.FromLocationId, "From", ct);
        entity.ToLocation = await RequireLocationAsync(dto.ToLocationId, "To", ct);
        var saved = await _journeyRepository.AddAsync(entity, ct);
        return _mapper.Map<JourneyDetailDto>(saved);
    }

    // ----- Stay & Meals -----

    public Task<IReadOnlyList<StayMealDto>> GetStayMealsAsync(long tourId, CancellationToken ct = default)
    {
        // ProjectTo for LocationName - same reason as GetJourneyAsync above.
        var items = _stayMealRepository.Query().Where(s => s.TourId == tourId).OrderBy(s => s.DayNumber)
            .ProjectTo<StayMealDto>(_mapper.ConfigurationProvider).ToList();
        return Task.FromResult<IReadOnlyList<StayMealDto>>(items);
    }

    public async Task<StayMealDto> AddStayMealAsync(long tourId, StayMealDto dto, CancellationToken ct = default)
    {
        await RequireTourAsync(tourId, ct);
        var entity = _mapper.Map<StayMeal>(dto);
        entity.TourId = tourId;
        // Optional here (a stay row can be hotel-only), but when supplied it is validated and
        // attached so the response carries LocationName - same reasoning as AddJourneyLegAsync.
        if (dto.LocationId is { } locationId)
        {
            entity.LocationEntity = await RequireLocationAsync(locationId, "Stay", ct);
        }
        var saved = await _stayMealRepository.AddAsync(entity, ct);
        return _mapper.Map<StayMealDto>(saved);
    }

    public async Task DeleteStayMealAsync(long tourId, long stayMealId, CancellationToken ct = default)
    {
        var entity = await RequireScoped(_stayMealRepository, stayMealId, tourId, s => s.TourId, ct, "Stay & meal entry");
        // Hard delete, like itinerary days: stay_meal has no status column, and GetStayMealsAsync
        // returns every row for the tour.
        await _stayMealRepository.DeleteAsync(entity, ct);
    }

    // ----- Content -----

    public Task<IReadOnlyList<TourContentDto>> GetContentAsync(long tourId, CancellationToken ct = default)
    {
        // status=true only, matching Java's findByTour_TourIdAndStatusTrue - this is the read half
        // of DeleteContentAsync's soft delete, and both backends share one database.
        var items = _contentRepository.Query().Where(c => c.TourId == tourId && c.Status).ToList();
        return Task.FromResult<IReadOnlyList<TourContentDto>>(_mapper.Map<List<TourContentDto>>(items));
    }

    public async Task<TourContentDto> UpsertContentAsync(long tourId, TourContentDto dto, CancellationToken ct = default)
    {
        await RequireTourAsync(tourId, ct);

        var contentType = Enum.Parse<Domain.Enums.TourContentType>(dto.ContentType);
        // A tour has at most one row per tab per language. The client may omit the language (the
        // admin "Good to know" form only ever sends English), so default it before the lookup -
        // otherwise the match fails and every save inserts a duplicate tab.
        var languageCode = string.IsNullOrWhiteSpace(dto.LanguageCode) ? "en" : dto.LanguageCode;

        var existing = _contentRepository.Query()
            .FirstOrDefault(c => c.TourId == tourId && c.ContentType == contentType && c.LanguageCode == languageCode);

        if (existing is not null)
        {
            existing.ContentText = dto.ContentText;
            // Always true on save, never dto.Status: saving a tab that was previously removed
            // revives it, which is what the admin form's "Save" means after a "Remove".
            existing.Status = true;
            await _contentRepository.UpdateAsync(existing, ct);
            return _mapper.Map<TourContentDto>(existing);
        }

        var entity = _mapper.Map<TourContent>(dto);
        entity.TourId = tourId;
        entity.LanguageCode = languageCode;
        entity.Status = true;
        var saved = await _contentRepository.AddAsync(entity, ct);
        return _mapper.Map<TourContentDto>(saved);
    }

    public async Task DeleteContentAsync(long tourId, long tourContentId, CancellationToken ct = default)
    {
        var entity = await RequireScoped(_contentRepository, tourContentId, tourId, c => c.TourId, ct, "Tour content");
        // Soft delete, consistent with media/add-ons and with GetContentAsync only returning
        // status=true rows.
        entity.Status = false;
        await _contentRepository.UpdateAsync(entity, ct);
    }

    // ----- Media -----

    public Task<IReadOnlyList<TourMediaDto>> GetMediaAsync(long tourId, CancellationToken ct = default)
    {
        var items = _mediaRepository.Query().Where(m => m.TourId == tourId && m.Status).OrderBy(m => m.DisplayOrder).ToList();
        return Task.FromResult<IReadOnlyList<TourMediaDto>>(_mapper.Map<List<TourMediaDto>>(items));
    }

    public async Task<TourMediaDto> AddMediaAsync(long tourId, TourMediaDto dto, CancellationToken ct = default)
    {
        await RequireTourAsync(tourId, ct);
        var entity = _mapper.Map<TourMedia>(dto);
        entity.TourId = tourId;
        var saved = await _mediaRepository.AddAsync(entity, ct);
        return _mapper.Map<TourMediaDto>(saved);
    }

    public async Task DeleteMediaAsync(long tourId, long mediaId, CancellationToken ct = default)
    {
        var entity = await RequireScoped(_mediaRepository, mediaId, tourId, m => m.TourId, ct, "Media");
        // Soft delete, matching Java: the row stays so an uploaded file is never orphaned by a
        // mis-click, and GetMediaAsync filters it out.
        entity.Status = false;
        await _mediaRepository.UpdateAsync(entity, ct);
    }

    // ----- Add-ons -----

    public Task<IReadOnlyList<TourAddonDto>> GetAddonsAsync(long tourId, CancellationToken ct = default)
    {
        var items = _addonRepository.Query().Where(a => a.TourId == tourId && a.Status).OrderBy(a => a.DisplayOrder).ToList();
        return Task.FromResult<IReadOnlyList<TourAddonDto>>(_mapper.Map<List<TourAddonDto>>(items));
    }

    public async Task<TourAddonDto> AddAddonAsync(long tourId, TourAddonDto dto, CancellationToken ct = default)
    {
        await RequireTourAsync(tourId, ct);
        var entity = _mapper.Map<TourAddon>(dto);
        entity.TourId = tourId;
        var saved = await _addonRepository.AddAsync(entity, ct);
        return _mapper.Map<TourAddonDto>(saved);
    }

    public async Task<TourAddonDto> UpdateAddonAsync(long tourId, long addonId, TourAddonDto dto, CancellationToken ct = default)
    {
        var entity = await RequireScoped(_addonRepository, addonId, tourId, a => a.TourId, ct, "Add-on");
        _mapper.Map(dto, entity);
        entity.TourId = tourId;
        await _addonRepository.UpdateAsync(entity, ct);
        return _mapper.Map<TourAddonDto>(entity);
    }

    public async Task DeleteAddonAsync(long tourId, long addonId, CancellationToken ct = default)
    {
        var entity = await RequireScoped(_addonRepository, addonId, tourId, a => a.TourId, ct, "Add-on");
        // Soft delete, matching Java. It also has to be a soft delete: bookings reference add-ons
        // by id, so removing the row would break the cost breakdown on past bookings.
        entity.Status = false;
        await _addonRepository.UpdateAsync(entity, ct);
    }

    // ----- Itinerary -----

    public Task<IReadOnlyList<ItineraryDto>> GetItineraryAsync(long tourId, CancellationToken ct = default)
    {
        var items = _itineraryRepository.Query().Where(i => i.TourId == tourId).OrderBy(i => i.DayNumber).ToList();
        return Task.FromResult<IReadOnlyList<ItineraryDto>>(_mapper.Map<List<ItineraryDto>>(items));
    }

    public async Task<ItineraryDto> AddItineraryDayAsync(long tourId, ItineraryDto dto, CancellationToken ct = default)
    {
        await RequireTourAsync(tourId, ct);
        var entity = _mapper.Map<Itinerary>(dto);
        entity.TourId = tourId;
        var saved = await _itineraryRepository.AddAsync(entity, ct);
        return _mapper.Map<ItineraryDto>(saved);
    }

    public async Task<ItineraryDto> UpdateItineraryDayAsync(long tourId, long itineraryId, ItineraryDto dto, CancellationToken ct = default)
    {
        var entity = await RequireScoped(_itineraryRepository, itineraryId, tourId, i => i.TourId, ct, "Itinerary day");
        _mapper.Map(dto, entity);
        entity.TourId = tourId;
        await _itineraryRepository.UpdateAsync(entity, ct);
        return _mapper.Map<ItineraryDto>(entity);
    }

    public async Task DeleteItineraryDayAsync(long tourId, long itineraryId, CancellationToken ct = default)
    {
        var entity = await RequireScoped(_itineraryRepository, itineraryId, tourId, i => i.TourId, ct, "Itinerary day");
        await _itineraryRepository.DeleteAsync(entity, ct);
    }

    private static async Task<TEntity> RequireScoped<TEntity>(IGenericRepository<TEntity, long> repository, long id,
        long tourId, Func<TEntity, long> tourIdSelector, CancellationToken ct, string label) where TEntity : class
    {
        var entity = await repository.GetByIdAsync(id, ct);
        if (entity is null || tourIdSelector(entity) != tourId)
        {
            throw new ResourceNotFoundException($"{label} {id} was not found for tour {tourId}");
        }
        return entity;
    }
}
