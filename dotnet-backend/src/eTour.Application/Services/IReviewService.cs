using eTour.Application.Dtos;

namespace eTour.Application.Services;

public interface IReviewService
{
    Task<IReadOnlyList<ReviewResponse>> GetAllAsync(CancellationToken ct = default);
    Task<ReviewResponse> GetByIdAsync(long id, CancellationToken ct = default);
    Task<ReviewResponse> AddReviewAsync(ReviewResponse review, CancellationToken ct = default);
    Task<ReviewResponse> EditReviewAsync(long id, ReviewRequest request, CancellationToken ct = default);
    Task DeleteReviewAsync(long id, CancellationToken ct = default);
    Task<IReadOnlyList<ReviewResponse>> GetByCustomerIdAsync(long customerId, CancellationToken ct = default);
    Task<IReadOnlyList<ReviewResponse>> GetByTourIdAsync(long tourId, CancellationToken ct = default);
    Task<IReadOnlyList<ReviewResponse>> GetByRatingAsync(int rating, CancellationToken ct = default);
    Task<IReadOnlyList<ReviewResponse>> SortByRatingAsync(CancellationToken ct = default);
    Task<IReadOnlyList<ReviewResponse>> Top5ReviewsAsync(long tourId, CancellationToken ct = default);
    Task<double> GetAverageRatingAsync(long tourId, CancellationToken ct = default);

    Task<ReviewResponse> AddMyReviewAsync(long tourId, ReviewRequest request, CancellationToken ct = default);
    Task<ReviewResponse> EditMyReviewAsync(long tourId, ReviewRequest request, CancellationToken ct = default);
    Task DeleteMyReviewAsync(long reviewId, CancellationToken ct = default);
}
