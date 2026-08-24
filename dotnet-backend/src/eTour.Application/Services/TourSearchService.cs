using eTour.Application.Common;
using eTour.Application.Dtos;
using eTour.Domain.Entities;
using eTour.Domain.Enums;

namespace eTour.Application.Services;

/// <summary>
/// BRD 3.6 search. Only ACTIVE tours are searchable. Result enrichment (departure dates,
/// thumbnail, rating) is done with three batch queries keyed on the page's tour ids, so a page
/// of N results costs a constant number of queries rather than 1 + 3N.
/// </summary>
public class TourSearchService : ITourSearchService
{
    private static readonly HashSet<string> SortableFields = ["tourId", "title", "basePrice", "durationDays"];

    private readonly IGenericRepository<Tour, long> _tourRepository;
    private readonly IGenericRepository<TourSchedule, long> _scheduleRepository;
    private readonly IGenericRepository<TourMedia, long> _mediaRepository;
    private readonly IGenericRepository<Review, long> _reviewRepository;

    public TourSearchService(IGenericRepository<Tour, long> tourRepository, IGenericRepository<TourSchedule, long> scheduleRepository,
        IGenericRepository<TourMedia, long> mediaRepository, IGenericRepository<Review, long> reviewRepository)
    {
        _tourRepository = tourRepository;
        _scheduleRepository = scheduleRepository;
        _mediaRepository = mediaRepository;
        _reviewRepository = reviewRepository;
    }

    public Task<PagedResult<TourSearchResultDto>> SearchAsync(TourSearchRequest request, CancellationToken ct = default)
    {
        var query = _tourRepository.Query().Where(t => t.Status == TourStatus.ACTIVE);

        if (!string.IsNullOrWhiteSpace(request.TourName))
        {
            var like = request.TourName.Trim().ToLower();
            query = query.Where(t => t.Title.ToLower().Contains(like));
        }

        if (!string.IsNullOrWhiteSpace(request.TourCode))
        {
            // An unknown code is treated as "no tour matches" rather than a 500.
            if (!Enum.TryParse<TourCode>(request.TourCode.Trim(), true, out var parsedCode))
            {
                return Task.FromResult(EmptyPage(request));
            }
            query = query.Where(t => t.TourCode == parsedCode);
        }

        if (request.CategoryId is { } categoryId)
        {
            query = query.Where(t => t.Categories.Any(c => c.CategoryId == categoryId));
        }
        if (request.MinPrice is { } minPrice)
        {
            query = query.Where(t => t.BasePrice >= minPrice);
        }
        if (request.MaxPrice is { } maxPrice)
        {
            query = query.Where(t => t.BasePrice <= maxPrice);
        }
        if (request.MinDuration is { } minDuration)
        {
            query = query.Where(t => t.DurationDays >= minDuration);
        }
        if (request.MaxDuration is { } maxDuration)
        {
            query = query.Where(t => t.DurationDays <= maxDuration);
        }

        // BRD 3.6 "Search on Period" - a tour matches if it has at least one schedule that both
        // departs on/after startDate and returns on/before endDate.
        if (request.StartDate is not null || request.EndDate is not null)
        {
            var start = request.StartDate;
            var end = request.EndDate;
            query = query.Where(t => t.Schedules.Any(s =>
                (start == null || s.DepartureDate >= start) && (end == null || s.ReturnDate <= end)));
        }

        var totalCount = query.LongCount();

        var sortBy = SortableFields.Contains(request.SortBy) ? request.SortBy : "tourId";
        var ascending = string.Equals(request.SortDir, "asc", StringComparison.OrdinalIgnoreCase);
        query = ApplySort(query, sortBy, ascending);

        var size = Math.Clamp(request.Size, 1, 50);
        var page = Math.Max(request.Page, 0);

        var tours = query.Skip(page * size).Take(size).ToList();
        if (tours.Count == 0)
        {
            return Task.FromResult(EmptyPage(request, totalCount));
        }

        var tourIds = tours.Select(t => t.TourId).ToList();

        var nearestSchedules = LoadNearestSchedules(tourIds, request.StartDate, request.EndDate);
        var thumbnails = LoadThumbnails(tourIds);
        var ratings = LoadRatingSummaries(tourIds);

        var items = tours.Select(tour =>
        {
            var dto = new TourSearchResultDto
            {
                TourId = tour.TourId,
                Title = tour.Title,
                Description = tour.Description,
                TourCode = tour.TourCode.ToString(),
                DurationDays = tour.DurationDays,
                BasePrice = tour.BasePrice,
                ImageUrl = thumbnails.GetValueOrDefault(tour.TourId)
            };

            if (nearestSchedules.TryGetValue(tour.TourId, out var schedule))
            {
                dto.ScheduleId = schedule.ScheduleId;
                dto.DepartureDate = schedule.DepartureDate;
                dto.ReturnDate = schedule.ReturnDate;
                dto.AvailableSeats = schedule.AvailableSeats;
                dto.SchedulePrice = schedule.Price;
            }

            if (ratings.TryGetValue(tour.TourId, out var rating))
            {
                dto.AverageRating = rating.Average;
                dto.TotalReviews = rating.Count;
            }

            return dto;
        }).ToList();

        return Task.FromResult(new PagedResult<TourSearchResultDto>
        {
            Content = items,
            Number = page,
            Size = size,
            TotalElements = totalCount,
            TotalPages = (int)Math.Ceiling(totalCount / (double)size)
        });
    }

    private static IQueryable<Tour> ApplySort(IQueryable<Tour> query, string sortBy, bool ascending) => sortBy switch
    {
        "title" => ascending ? query.OrderBy(t => t.Title) : query.OrderByDescending(t => t.Title),
        "basePrice" => ascending ? query.OrderBy(t => t.BasePrice) : query.OrderByDescending(t => t.BasePrice),
        "durationDays" => ascending ? query.OrderBy(t => t.DurationDays) : query.OrderByDescending(t => t.DurationDays),
        _ => ascending ? query.OrderBy(t => t.TourId) : query.OrderByDescending(t => t.TourId)
    };

    /// <summary>One query for the whole page; keeps the earliest schedule per tour.</summary>
    private Dictionary<long, TourSchedule> LoadNearestSchedules(List<long> tourIds, DateOnly? startDate, DateOnly? endDate)
    {
        // With no explicit start date, "nearest" means the next departure from today.
        var from = startDate ?? DateOnly.FromDateTime(DateTime.UtcNow);

        var candidates = _scheduleRepository.Query()
            .Where(s => tourIds.Contains(s.TourId) && s.DepartureDate >= from && (endDate == null || s.ReturnDate <= endDate))
            .OrderBy(s => s.DepartureDate)
            .ToList();

        var byTour = new Dictionary<long, TourSchedule>();
        foreach (var schedule in candidates)
        {
            byTour.TryAdd(schedule.TourId, schedule);
        }
        return byTour;
    }

    /// <summary>One query; first IMAGE by display order wins as the card thumbnail.</summary>
    private Dictionary<long, string> LoadThumbnails(List<long> tourIds)
    {
        var candidates = _mediaRepository.Query()
            .Where(m => tourIds.Contains(m.TourId) && m.Status && m.MediaType == MediaType.IMAGE)
            .OrderBy(m => m.TourId).ThenBy(m => m.DisplayOrder)
            .ToList();

        var byTour = new Dictionary<long, string>();
        foreach (var media in candidates)
        {
            byTour.TryAdd(media.TourId, media.FilePath);
        }
        return byTour;
    }

    /// <summary>One grouped query -&gt; {tourId: (avgRating, reviewCount)}.</summary>
    private Dictionary<long, (double Average, long Count)> LoadRatingSummaries(List<long> tourIds)
    {
        return _reviewRepository.Query()
            .Where(r => tourIds.Contains(r.TourId))
            .GroupBy(r => r.TourId)
            .Select(g => new { TourId = g.Key, Average = g.Average(r => (double)r.Rating), Count = g.LongCount() })
            .ToDictionary(x => x.TourId, x => (x.Average, x.Count));
    }

    private static PagedResult<TourSearchResultDto> EmptyPage(TourSearchRequest request, long totalCount = 0) => new()
    {
        Content = [],
        Number = Math.Max(request.Page, 0),
        Size = Math.Clamp(request.Size, 1, 50),
        TotalElements = totalCount,
        TotalPages = 0
    };
}
