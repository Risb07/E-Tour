using AutoMapper;
using AutoMapper.QueryableExtensions;
using eTour.Application.Common;
using eTour.Application.Dtos;
using eTour.Domain.Entities;
using eTour.Domain.Exceptions;

namespace eTour.Application.Services;

public class WishlistService : IWishlistService
{
    private readonly IGenericRepository<WishlistItem, long> _repository;
    private readonly IGenericRepository<Tour, long> _tourRepository;
    private readonly ICurrentUserService _currentUser;
    private readonly IMapper _mapper;

    public WishlistService(IGenericRepository<WishlistItem, long> repository, IGenericRepository<Tour, long> tourRepository,
        ICurrentUserService currentUser, IMapper mapper)
    {
        _repository = repository;
        _tourRepository = tourRepository;
        _currentUser = currentUser;
        _mapper = mapper;
    }

    public async Task<IReadOnlyList<WishlistItemResponse>> GetMyWishlistAsync(CancellationToken ct = default)
    {
        var customer = await _currentUser.CurrentCustomerAsync(ct);
        // ProjectTo (not Map) so the Tour.Title/BasePrice join is translated into SQL - the
        // generic repository never eager-loads navigations, so an in-memory Map would NPE.
        return _repository.Query().Where(w => w.CustomerId == customer.CustomerId)
            .ProjectTo<WishlistItemResponse>(_mapper.ConfigurationProvider).ToList();
    }

    public async Task<bool> IsSavedAsync(long tourId, CancellationToken ct = default)
    {
        var customer = await _currentUser.CurrentCustomerAsync(ct);
        return _repository.Query().Any(w => w.CustomerId == customer.CustomerId && w.TourId == tourId);
    }

    public async Task<WishlistItemResponse> AddAsync(long tourId, CancellationToken ct = default)
    {
        var customer = await _currentUser.CurrentCustomerAsync(ct);

        var existingId = _repository.Query()
            .Where(w => w.CustomerId == customer.CustomerId && w.TourId == tourId)
            .Select(w => (long?)w.WishlistItemId).FirstOrDefault();

        long wishlistItemId;
        if (existingId is { } id)
        {
            wishlistItemId = id;
        }
        else
        {
            _ = await _tourRepository.GetByIdAsync(tourId, ct) ?? throw new ResourceNotFoundException($"Tour {tourId} was not found");
            var entity = new WishlistItem { CustomerId = customer.CustomerId, TourId = tourId, CreatedAt = DateTime.UtcNow };
            var saved = await _repository.AddAsync(entity, ct);
            wishlistItemId = saved.WishlistItemId;
        }

        return _repository.Query().Where(w => w.WishlistItemId == wishlistItemId)
            .ProjectTo<WishlistItemResponse>(_mapper.ConfigurationProvider).First();
    }

    public async Task RemoveAsync(long tourId, CancellationToken ct = default)
    {
        var customer = await _currentUser.CurrentCustomerAsync(ct);
        var existing = _repository.Query().FirstOrDefault(w => w.CustomerId == customer.CustomerId && w.TourId == tourId);
        if (existing is not null)
        {
            await _repository.DeleteAsync(existing, ct);
        }
    }
}
