using AutoMapper;
using AutoMapper.QueryableExtensions;
using eTour.Application.Common;
using eTour.Application.Dtos;
using eTour.Domain.Entities;
using eTour.Domain.Enums;
using eTour.Domain.Exceptions;

namespace eTour.Application.Services;

public class ReviewService : IReviewService
{
    private readonly IGenericRepository<Review, long> _reviewRepository;
    private readonly IGenericRepository<Tour, long> _tourRepository;
    private readonly IGenericRepository<Customer, long> _customerRepository;
    private readonly IGenericRepository<Booking, long> _bookingRepository;
    private readonly ICurrentUserService _currentUser;
    private readonly IMapper _mapper;

    public ReviewService(IGenericRepository<Review, long> reviewRepository, IGenericRepository<Tour, long> tourRepository,
        IGenericRepository<Customer, long> customerRepository, IGenericRepository<Booking, long> bookingRepository,
        ICurrentUserService currentUser, IMapper mapper)
    {
        _reviewRepository = reviewRepository;
        _tourRepository = tourRepository;
        _customerRepository = customerRepository;
        _bookingRepository = bookingRepository;
        _currentUser = currentUser;
        _mapper = mapper;
    }

    private IQueryable<ReviewResponse> Projected => _reviewRepository.Query().ProjectTo<ReviewResponse>(_mapper.ConfigurationProvider);

    public Task<IReadOnlyList<ReviewResponse>> GetAllAsync(CancellationToken ct = default) =>
        Task.FromResult<IReadOnlyList<ReviewResponse>>(Projected.ToList());

    public Task<ReviewResponse> GetByIdAsync(long id, CancellationToken ct = default)
    {
        var review = Projected.FirstOrDefault(r => r.ReviewId == id) ?? throw new ResourceNotFoundException("Review not Found");
        return Task.FromResult(review);
    }

    public async Task<ReviewResponse> AddReviewAsync(ReviewResponse review, CancellationToken ct = default)
    {
        var entity = new Review { TourId = review.TourId, CustomerId = review.CustomerId, Rating = review.Rating, Comment = review.Comment };
        var saved = await _reviewRepository.AddAsync(entity, ct);
        return await GetByIdAsync(saved.ReviewId, ct);
    }

    public async Task<ReviewResponse> EditReviewAsync(long id, ReviewRequest request, CancellationToken ct = default)
    {
        var review = await _reviewRepository.GetByIdAsync(id, ct) ?? throw new ResourceNotFoundException("Review Not Found");
        review.Rating = request.Rating;
        review.Comment = request.Comment;
        await _reviewRepository.UpdateAsync(review, ct);
        return await GetByIdAsync(id, ct);
    }

    public async Task DeleteReviewAsync(long id, CancellationToken ct = default)
    {
        var review = await _reviewRepository.GetByIdAsync(id, ct) ?? throw new ResourceNotFoundException("Review Not Found");
        await _reviewRepository.DeleteAsync(review, ct);
    }

    public async Task<IReadOnlyList<ReviewResponse>> GetByCustomerIdAsync(long customerId, CancellationToken ct = default)
    {
        _ = await _customerRepository.GetByIdAsync(customerId, ct) ?? throw new ResourceNotFoundException("Customer Not Found");
        // A customer who simply hasn't reviewed anything is not an error.
        return Projected.Where(r => r.CustomerId == customerId).ToList();
    }

    public async Task<IReadOnlyList<ReviewResponse>> GetByTourIdAsync(long tourId, CancellationToken ct = default)
    {
        _ = await _tourRepository.GetByIdAsync(tourId, ct) ?? throw new ResourceNotFoundException("Tour Not Found");
        return Projected.Where(r => r.TourId == tourId).ToList();
    }

    public Task<IReadOnlyList<ReviewResponse>> GetByRatingAsync(int rating, CancellationToken ct = default) =>
        Task.FromResult<IReadOnlyList<ReviewResponse>>(Projected.Where(r => r.Rating == rating).ToList());

    public Task<IReadOnlyList<ReviewResponse>> SortByRatingAsync(CancellationToken ct = default) =>
        Task.FromResult<IReadOnlyList<ReviewResponse>>(Projected.OrderByDescending(r => r.Rating).ToList());

    public Task<IReadOnlyList<ReviewResponse>> Top5ReviewsAsync(long tourId, CancellationToken ct = default) =>
        Task.FromResult<IReadOnlyList<ReviewResponse>>(Projected.Where(r => r.TourId == tourId).Take(5).ToList());

    public Task<double> GetAverageRatingAsync(long tourId, CancellationToken ct = default)
    {
        var ratings = _reviewRepository.Query().Where(r => r.TourId == tourId).Select(r => r.Rating).ToList();
        return Task.FromResult(ratings.Count == 0 ? 0d : ratings.Average());
    }

    /// <summary>A review is only allowed from someone who actually booked the tour (a cancelled booking doesn't earn the right).</summary>
    private void RequireBookingFor(long customerId, long tourId)
    {
        var hasBooked = _bookingRepository.Query().Any(b => b.CustomerId == customerId && b.Schedule.TourId == tourId &&
            (b.BookingStatus == BookingStatus.PENDING || b.BookingStatus == BookingStatus.CONFIRMED || b.BookingStatus == BookingStatus.COMPLETED));
        if (!hasBooked)
        {
            throw new IllegalOperationException("You can only review a tour you have booked");
        }
    }

    public async Task<ReviewResponse> AddMyReviewAsync(long tourId, ReviewRequest request, CancellationToken ct = default)
    {
        var customer = await _currentUser.CurrentCustomerAsync(ct);
        _ = await _tourRepository.GetByIdAsync(tourId, ct) ?? throw new ResourceNotFoundException("Tour Not Found");
        RequireBookingFor(customer.CustomerId, tourId);

        if (_reviewRepository.Query().Any(r => r.CustomerId == customer.CustomerId && r.TourId == tourId))
        {
            throw new IllegalOperationException("You have already reviewed this tour");
        }

        var review = new Review { CustomerId = customer.CustomerId, TourId = tourId, Rating = request.Rating, Comment = request.Comment };
        var saved = await _reviewRepository.AddAsync(review, ct);
        return await GetByIdAsync(saved.ReviewId, ct);
    }

    public async Task<ReviewResponse> EditMyReviewAsync(long tourId, ReviewRequest request, CancellationToken ct = default)
    {
        var customer = await _currentUser.CurrentCustomerAsync(ct);
        _ = await _tourRepository.GetByIdAsync(tourId, ct) ?? throw new ResourceNotFoundException("Tour Not Found");
        var review = _reviewRepository.Query().FirstOrDefault(r => r.CustomerId == customer.CustomerId && r.TourId == tourId)
            ?? throw new ResourceNotFoundException("Review not found");

        review.Rating = request.Rating;
        review.Comment = request.Comment;
        await _reviewRepository.UpdateAsync(review, ct);
        return await GetByIdAsync(review.ReviewId, ct);
    }

    public async Task DeleteMyReviewAsync(long reviewId, CancellationToken ct = default)
    {
        var review = await _reviewRepository.GetByIdAsync(reviewId, ct) ?? throw new ResourceNotFoundException("Review Not Found");
        if (!_currentUser.IsAdmin)
        {
            var customer = await _currentUser.CurrentCustomerAsync(ct);
            if (review.CustomerId != customer.CustomerId)
            {
                // 404 on purpose - don't reveal the review exists to a non-owner.
                throw new ResourceNotFoundException("Review Not Found");
            }
        }
        await _reviewRepository.DeleteAsync(review, ct);
    }
}
