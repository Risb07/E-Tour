using AutoMapper;
using eTour.Application.Common;
using eTour.Application.Dtos;
using eTour.Domain.Entities;
using eTour.Domain.Exceptions;

namespace eTour.Application.Services;

public class TourScheduleService : ITourScheduleService
{
    private readonly IGenericRepository<TourSchedule, long> _repository;
    private readonly IGenericRepository<Tour, long> _tourRepository;
    private readonly IMapper _mapper;

    public TourScheduleService(IGenericRepository<TourSchedule, long> repository, IGenericRepository<Tour, long> tourRepository,
        IMapper mapper)
    {
        _repository = repository;
        _tourRepository = tourRepository;
        _mapper = mapper;
    }

    public async Task<IReadOnlyList<TourScheduleDto>> GetAllAsync(CancellationToken ct = default) =>
        _mapper.Map<List<TourScheduleDto>>(await _repository.GetAllAsync(ct));

    public async Task<TourScheduleDto> GetByIdAsync(long id, CancellationToken ct = default) =>
        _mapper.Map<TourScheduleDto>(await RequireAsync(id, ct));

    public Task<IReadOnlyList<TourScheduleDto>> GetByTourIdAsync(long tourId, CancellationToken ct = default)
    {
        var schedules = _repository.Query().Where(s => s.TourId == tourId).ToList();
        return Task.FromResult<IReadOnlyList<TourScheduleDto>>(_mapper.Map<List<TourScheduleDto>>(schedules));
    }

    public async Task<TourScheduleDto> CreateForTourAsync(long tourId, TourScheduleDto dto, CancellationToken ct = default)
    {
        _ = await _tourRepository.GetByIdAsync(tourId, ct) ?? throw new ResourceNotFoundException($"Tour {tourId} was not found");
        var entity = _mapper.Map<TourSchedule>(dto);
        entity.TourId = tourId;
        entity.ValidateDates();
        var saved = await _repository.AddAsync(entity, ct);
        return _mapper.Map<TourScheduleDto>(saved);
    }

    public async Task<TourScheduleDto> UpdateAsync(long scheduleId, long tourId, TourScheduleDto dto, CancellationToken ct = default)
    {
        var entity = await RequireAsync(scheduleId, ct);
        _mapper.Map(dto, entity);
        entity.TourId = tourId;
        entity.ValidateDates();
        await _repository.UpdateAsync(entity, ct);
        return _mapper.Map<TourScheduleDto>(entity);
    }

    public async Task DeleteAsync(long id, CancellationToken ct = default)
    {
        var entity = await RequireAsync(id, ct);
        await _repository.DeleteAsync(entity, ct);
    }

    private async Task<TourSchedule> RequireAsync(long id, CancellationToken ct) =>
        await _repository.GetByIdAsync(id, ct) ?? throw new ResourceNotFoundException($"Tour schedule {id} was not found");
}
