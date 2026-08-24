using AutoMapper;
using eTour.Application.Common;
using eTour.Application.Dtos;
using eTour.Domain.Entities;
using eTour.Domain.Exceptions;

namespace eTour.Application.Services;

public class RoomChargeService : IRoomChargeService
{
    private readonly IGenericService<RoomCharge, RoomChargeDto, long> _generic;
    private readonly IGenericRepository<RoomCharge, long> _repository;
    private readonly IGenericRepository<Tour, long> _tourRepository;
    private readonly IMapper _mapper;

    public RoomChargeService(IGenericService<RoomCharge, RoomChargeDto, long> generic,
        IGenericRepository<RoomCharge, long> repository, IGenericRepository<Tour, long> tourRepository, IMapper mapper)
    {
        _generic = generic;
        _repository = repository;
        _tourRepository = tourRepository;
        _mapper = mapper;
    }

    public Task<IReadOnlyList<RoomChargeDto>> GetByTourIdAsync(long tourId, CancellationToken ct = default)
    {
        var charges = _repository.Query().Where(rc => rc.TourId == tourId && rc.Active).ToList();
        return Task.FromResult<IReadOnlyList<RoomChargeDto>>(_mapper.Map<List<RoomChargeDto>>(charges));
    }

    public async Task<RoomChargeDto> CreateForTourAsync(long tourId, RoomChargeDto dto, CancellationToken ct = default)
    {
        _ = await _tourRepository.GetByIdAsync(tourId, ct) ?? throw new ResourceNotFoundException($"Tour {tourId} was not found");
        dto.TourId = tourId;
        return await _generic.CreateAsync(dto, ct);
    }

    public Task<RoomChargeDto> UpdateAsync(long id, RoomChargeDto dto, CancellationToken ct = default) =>
        _generic.UpdateAsync(id, dto, ct);

    public Task DeleteAsync(long id, CancellationToken ct = default) => _generic.DeleteAsync(id, ct);
}
