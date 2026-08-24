using AutoMapper;
using eTour.Application.Common;
using eTour.Application.Dtos;
using eTour.Domain.Entities;
using eTour.Domain.Exceptions;

namespace eTour.Application.Services;

public class NewsletterService : INewsletterService
{
    private readonly IGenericRepository<NewsletterSubscriber, long> _repository;
    private readonly IMapper _mapper;

    public NewsletterService(IGenericRepository<NewsletterSubscriber, long> repository, IMapper mapper)
    {
        _repository = repository;
        _mapper = mapper;
    }

    public async Task<NewsletterResponse> SubscribeAsync(NewsletterRequest request, CancellationToken ct = default)
    {
        var existing = _repository.Query().FirstOrDefault(s => s.Email == request.Email);
        if (existing is not null)
        {
            // Idempotent: re-subscribing a previously unsubscribed address just reactivates it.
            existing.Active = true;
            existing.UnsubscribedAt = null;
            existing.Name = request.Name ?? existing.Name;
            await _repository.UpdateAsync(existing, ct);
            return _mapper.Map<NewsletterResponse>(existing);
        }

        var entity = _mapper.Map<NewsletterSubscriber>(request);
        entity.Active = true;
        entity.SubscribedAt = DateTime.UtcNow;
        var saved = await _repository.AddAsync(entity, ct);
        return _mapper.Map<NewsletterResponse>(saved);
    }

    public async Task UnsubscribeAsync(string email, CancellationToken ct = default)
    {
        var existing = _repository.Query().FirstOrDefault(s => s.Email == email)
            ?? throw new ResourceNotFoundException("Subscriber not found");
        existing.Active = false;
        existing.UnsubscribedAt = DateTime.UtcNow;
        await _repository.UpdateAsync(existing, ct);
    }

    public async Task<IReadOnlyList<NewsletterResponse>> GetAllAsync(CancellationToken ct = default)
    {
        var entities = await _repository.GetAllAsync(ct);
        return _mapper.Map<IReadOnlyList<NewsletterResponse>>(entities);
    }

    public async Task<long> CountAsync(CancellationToken ct = default) => (await _repository.GetAllAsync(ct)).Count;

    public async Task DeleteAsync(long id, CancellationToken ct = default)
    {
        var entity = await _repository.GetByIdAsync(id, ct)
            ?? throw new ResourceNotFoundException($"Subscriber {id} was not found");
        await _repository.DeleteAsync(entity, ct);
    }
}
