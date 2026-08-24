using eTour.Application.Dtos;

namespace eTour.Application.Services;

public interface INewsletterService
{
    Task<NewsletterResponse> SubscribeAsync(NewsletterRequest request, CancellationToken ct = default);
    Task UnsubscribeAsync(string email, CancellationToken ct = default);
    Task<IReadOnlyList<NewsletterResponse>> GetAllAsync(CancellationToken ct = default);
    Task<long> CountAsync(CancellationToken ct = default);
    Task DeleteAsync(long id, CancellationToken ct = default);
}
