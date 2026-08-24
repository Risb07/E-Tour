using AutoMapper;
using AutoMapper.QueryableExtensions;
using eTour.Application.Common;
using eTour.Application.Dtos;
using eTour.Domain.Entities;
using eTour.Domain.Enums;
using eTour.Domain.Exceptions;

namespace eTour.Application.Services;

public class TourService : ITourService
{
    private readonly IGenericService<Tour, TourResponse, long> _generic;
    private readonly IGenericRepository<Tour, long> _tourRepository;
    private readonly IGenericRepository<Category, long> _categoryRepository;
    private readonly IGenericRepository<TourSchedule, long> _scheduleRepository;
    private readonly IGenericRepository<Itinerary, long> _itineraryRepository;
    private readonly IGenericRepository<TourMedia, long> _mediaRepository;
    private readonly IGenericRepository<TourAddon, long> _addonRepository;
    private readonly IGenericRepository<Review, long> _reviewRepository;
    private readonly IMapper _mapper;

    public TourService(IGenericService<Tour, TourResponse, long> generic, IGenericRepository<Tour, long> tourRepository,
        IGenericRepository<Category, long> categoryRepository, IGenericRepository<TourSchedule, long> scheduleRepository,
        IGenericRepository<Itinerary, long> itineraryRepository, IGenericRepository<TourMedia, long> mediaRepository,
        IGenericRepository<TourAddon, long> addonRepository, IGenericRepository<Review, long> reviewRepository, IMapper mapper)
    {
        _generic = generic;
        _tourRepository = tourRepository;
        _categoryRepository = categoryRepository;
        _scheduleRepository = scheduleRepository;
        _itineraryRepository = itineraryRepository;
        _mediaRepository = mediaRepository;
        _addonRepository = addonRepository;
        _reviewRepository = reviewRepository;
        _mapper = mapper;
    }

    // Delete is plain CRUD, so it delegates straight to the generic service - the
    // "entity-specific service calls the generic service" composition requirement #9 asks for.
    // GetAll/GetById use ProjectTo over the repository instead of the generic service's Map,
    // because TourResponse.Categories comes from a join the generic service's plain
    // Repository.GetByIdAsync (no eager-load) can't populate correctly.
    public Task<IReadOnlyList<TourResponse>> GetAllAsync(CancellationToken ct = default) =>
        Task.FromResult<IReadOnlyList<TourResponse>>(
            _tourRepository.Query().ProjectTo<TourResponse>(_mapper.ConfigurationProvider).ToList());

    public Task<TourResponse> GetByIdAsync(long id, CancellationToken ct = default)
    {
        var response = _tourRepository.Query().Where(t => t.TourId == id)
            .ProjectTo<TourResponse>(_mapper.ConfigurationProvider).FirstOrDefault();
        return response is null
            ? throw new ResourceNotFoundException($"Tour not found with id: {id}")
            : Task.FromResult(response);
    }

    public Task DeleteAsync(long id, CancellationToken ct = default) => _generic.DeleteAsync(id, ct);

    public Task<IReadOnlyList<TourResponse>> GetByTourCodeAsync(string code, CancellationToken ct = default)
    {
        if (string.IsNullOrWhiteSpace(code))
        {
            throw new ArgumentException("Tour code cannot be null or empty");
        }
        if (!Enum.TryParse<TourCode>(code.Trim(), true, out var parsed))
        {
            throw new ArgumentException($"Invalid tour code '{code}'");
        }

        var tours = _tourRepository.Query().Where(t => t.TourCode == parsed)
            .ProjectTo<TourResponse>(_mapper.ConfigurationProvider).ToList();
        if (tours.Count == 0)
        {
            throw new ResourceNotFoundException($"No tours found with tour code: {code}");
        }
        return Task.FromResult<IReadOnlyList<TourResponse>>(tours);
    }

    // Create/Update carry tour-specific business logic (category linking) on top of the
    // generic repository, rather than the generic service's Create/Update - those assume one
    // symmetric DTO shape, but TourRequest (write) and TourResponse (read) intentionally differ.
    public async Task<TourResponse> CreateAsync(TourRequest request, CancellationToken ct = default)
    {
        var entity = _mapper.Map<Tour>(request);
        entity.Categories = await ResolveCategoriesAsync(request.Categories, ct);
        var saved = await _tourRepository.AddAsync(entity, ct);
        return _mapper.Map<TourResponse>(saved);
    }

    public async Task<TourResponse> UpdateAsync(long id, TourRequest request, CancellationToken ct = default)
    {
        var entity = await _tourRepository.GetByIdAsync(id, ct) ?? throw new ResourceNotFoundException($"Tour not found with id: {id}");

        entity.Title = request.Title;
        entity.Description = request.Description;
        entity.DurationDays = request.DurationDays;
        entity.BasePrice = request.BasePrice;
        entity.TourCode = Enum.Parse<TourCode>(request.TourCode, true);
        entity.Status = Enum.Parse<TourStatus>(request.Status, true);

        // The Categories nav was never loaded by the plain GetByIdAsync above, so without
        // loading it first EF has no "before" snapshot to diff against: reassigning the
        // collection then only ever generates INSERTs (throwing a duplicate-key error on any
        // category that was already linked) and never DELETEs a category that was removed.
        await _tourRepository.LoadCollectionAsync(entity, t => t.Categories, ct);
        entity.Categories = await ResolveCategoriesAsync(request.Categories, ct);

        await _tourRepository.UpdateAsync(entity, ct);
        return _mapper.Map<TourResponse>(entity);
    }

    public async Task<TourDetailsResponse> GetTourDetailsAsync(long id, CancellationToken ct = default)
    {
        var tour = await _tourRepository.GetByIdAsync(id, ct) ?? throw new ResourceNotFoundException($"Tour not found with id: {id}");

        var schedules = _scheduleRepository.Query().Where(s => s.TourId == id).ToList();
        var itinerary = _itineraryRepository.Query().Where(i => i.TourId == id).OrderBy(i => i.DayNumber).ToList();
        // status=true only, so this aggregate agrees with the per-tab /media and /addons endpoints -
        // both are soft deletes, and a removed image reappearing on the tour page while the admin
        // gallery shows it gone is exactly the kind of split-brain that is hard to spot.
        var media = _mediaRepository.Query().Where(m => m.TourId == id && m.Status).OrderBy(m => m.DisplayOrder).ToList();
        var addons = _addonRepository.Query().Where(a => a.TourId == id && a.Status).ToList();
        var reviews = _reviewRepository.Query().Where(r => r.TourId == id).ToList();
        // ProjectTo so the Customer.FullName join is translated into SQL - Query() never
        // eager-loads navigations, so mapping the already-materialized `reviews` above in-memory
        // would NPE on Customer.
        var reviewResponses = _reviewRepository.Query().Where(r => r.TourId == id)
            .ProjectTo<ReviewResponse>(_mapper.ConfigurationProvider).ToList();

        var reviewSummary = new ReviewSummary
        {
            TotalReviews = reviews.Count,
            AverageRating = reviews.Count == 0 ? 0 : reviews.Average(r => r.Rating)
        };

        return new TourDetailsResponse
        {
            Tour = _mapper.Map<TourResponse>(tour),
            Schedules = _mapper.Map<List<TourScheduleDto>>(schedules),
            Itinerary = _mapper.Map<List<ItineraryDto>>(itinerary),
            Media = _mapper.Map<List<TourMediaDto>>(media),
            Addons = _mapper.Map<List<TourAddonDto>>(addons),
            Reviews = reviewResponses,
            ReviewSummary = reviewSummary
        };
    }

    private async Task<List<Category>> ResolveCategoriesAsync(List<CategoryRef> refs, CancellationToken ct)
    {
        var categories = new List<Category>();
        foreach (var categoryId in refs.Select(c => c.CategoryId).Distinct())
        {
            var category = await _categoryRepository.GetByIdAsync(categoryId, ct)
                ?? throw new ResourceNotFoundException($"Category not found with id: {categoryId}");
            categories.Add(category);
        }
        return categories;
    }
}
