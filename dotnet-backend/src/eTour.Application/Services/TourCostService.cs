using AutoMapper;
using eTour.Application.Common;
using eTour.Application.Dtos;
using eTour.Domain.Entities;
using eTour.Domain.Exceptions;

namespace eTour.Application.Services;

public class TourCostService : ITourCostService
{
    private readonly IGenericService<TourCost, TourCostDto, long> _generic;
    private readonly IGenericRepository<TourCost, long> _repository;
    private readonly IGenericRepository<Tour, long> _tourRepository;
    private readonly IMapper _mapper;

    public TourCostService(IGenericService<TourCost, TourCostDto, long> generic,
        IGenericRepository<TourCost, long> repository, IGenericRepository<Tour, long> tourRepository, IMapper mapper)
    {
        _generic = generic;
        _repository = repository;
        _tourRepository = tourRepository;
        _mapper = mapper;
    }

    public Task<IReadOnlyList<TourCostDto>> GetAllAsync(CancellationToken ct = default) => _generic.GetAllAsync(ct);

    public Task<TourCostDto> GetByIdAsync(long id, CancellationToken ct = default) => _generic.GetByIdAsync(id, ct);

    public Task<IReadOnlyList<TourCostDto>> GetByTourIdAsync(long tourId, CancellationToken ct = default)
    {
        var costs = _repository.Query().Where(c => c.TourId == tourId).ToList();
        return Task.FromResult<IReadOnlyList<TourCostDto>>(_mapper.Map<List<TourCostDto>>(costs));
    }

    public async Task<TourCostDto> CreateForTourAsync(long tourId, TourCostDto dto, CancellationToken ct = default)
    {
        _ = await _tourRepository.GetByIdAsync(tourId, ct) ?? throw new ResourceNotFoundException($"Tour {tourId} was not found");
        dto.TourId = tourId;
        return await _generic.CreateAsync(dto, ct);
    }

    public async Task<TourCostDto> UpdateAsync(long costId, long tourId, TourCostDto dto, CancellationToken ct = default)
    {
        dto.TourId = tourId;
        return await _generic.UpdateAsync(costId, dto, ct);
    }

    public Task DeleteAsync(long id, CancellationToken ct = default) => _generic.DeleteAsync(id, ct);
}
