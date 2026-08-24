using AutoMapper;
using eTour.Application.Common;
using eTour.Application.Dtos;
using eTour.Domain.Entities;
using eTour.Domain.Exceptions;

namespace eTour.Application.Services;

public class SectorService : ISectorService
{
    private readonly IGenericService<Sector, SectorDto, long> _generic;
    private readonly IGenericRepository<SubSector, long> _subSectorRepository;
    private readonly IGenericRepository<TourProduct, long> _productRepository;
    private readonly IMapper _mapper;

    public SectorService(IGenericService<Sector, SectorDto, long> generic, IGenericRepository<SubSector, long> subSectorRepository,
        IGenericRepository<TourProduct, long> productRepository, IMapper mapper)
    {
        _generic = generic;
        _subSectorRepository = subSectorRepository;
        _productRepository = productRepository;
        _mapper = mapper;
    }

    public Task<IReadOnlyList<SectorDto>> GetAllAsync(CancellationToken ct = default) => _generic.GetAllAsync(ct);
    public Task<SectorDto> GetByIdAsync(long id, CancellationToken ct = default) => _generic.GetByIdAsync(id, ct);

    public Task<IReadOnlyList<SubSectorDto>> GetSubSectorsAsync(long sectorId, CancellationToken ct = default)
    {
        var subSectors = _subSectorRepository.Query().Where(s => s.SectorId == sectorId).ToList();
        return Task.FromResult<IReadOnlyList<SubSectorDto>>(_mapper.Map<List<SubSectorDto>>(subSectors));
    }

    public Task<IReadOnlyList<TourProductDto>> GetProductsAsync(long sectorId, CancellationToken ct = default)
    {
        var products = _productRepository.Query().Where(p => p.SubSector.SectorId == sectorId).ToList();
        return Task.FromResult<IReadOnlyList<TourProductDto>>(_mapper.Map<List<TourProductDto>>(products));
    }

    public Task<SectorDto> CreateAsync(SectorDto dto, CancellationToken ct = default) => _generic.CreateAsync(dto, ct);
    public Task<SectorDto> UpdateAsync(long id, SectorDto dto, CancellationToken ct = default) => _generic.UpdateAsync(id, dto, ct);
    public Task DeleteAsync(long id, CancellationToken ct = default) => _generic.DeleteAsync(id, ct);
}

public class SubSectorService : ISubSectorService
{
    private readonly IGenericService<SubSector, SubSectorDto, long> _generic;
    private readonly IGenericRepository<TourProduct, long> _productRepository;
    private readonly IMapper _mapper;

    public SubSectorService(IGenericService<SubSector, SubSectorDto, long> generic,
        IGenericRepository<TourProduct, long> productRepository, IMapper mapper)
    {
        _generic = generic;
        _productRepository = productRepository;
        _mapper = mapper;
    }

    public Task<IReadOnlyList<SubSectorDto>> GetAllAsync(CancellationToken ct = default) => _generic.GetAllAsync(ct);
    public Task<SubSectorDto> GetByIdAsync(long id, CancellationToken ct = default) => _generic.GetByIdAsync(id, ct);

    public Task<IReadOnlyList<TourProductDto>> GetProductsAsync(long subSectorId, CancellationToken ct = default)
    {
        var products = _productRepository.Query().Where(p => p.SubSectorId == subSectorId).ToList();
        return Task.FromResult<IReadOnlyList<TourProductDto>>(_mapper.Map<List<TourProductDto>>(products));
    }

    public Task<SubSectorDto> CreateAsync(SubSectorDto dto, CancellationToken ct = default) => _generic.CreateAsync(dto, ct);
    public Task<SubSectorDto> UpdateAsync(long id, SubSectorDto dto, CancellationToken ct = default) => _generic.UpdateAsync(id, dto, ct);
    public Task DeleteAsync(long id, CancellationToken ct = default) => _generic.DeleteAsync(id, ct);
}

public class TourProductService : ITourProductService
{
    private readonly IGenericService<TourProduct, TourProductDto, long> _generic;
    private readonly IGenericRepository<Tour, long> _tourRepository;

    public TourProductService(IGenericService<TourProduct, TourProductDto, long> generic, IGenericRepository<Tour, long> tourRepository)
    {
        _generic = generic;
        _tourRepository = tourRepository;
    }

    public Task<IReadOnlyList<TourProductDto>> GetAllAsync(CancellationToken ct = default) => _generic.GetAllAsync(ct);
    public Task<TourProductDto> GetByIdAsync(long id, CancellationToken ct = default) => _generic.GetByIdAsync(id, ct);

    public async Task<TourProductDto> CreateAsync(TourProductDto dto, CancellationToken ct = default)
    {
        await ResolveTourAsync(dto, ct);
        return await _generic.CreateAsync(dto, ct);
    }

    public async Task<TourProductDto> UpdateAsync(long id, TourProductDto dto, CancellationToken ct = default)
    {
        await ResolveTourAsync(dto, ct);
        return await _generic.UpdateAsync(id, dto, ct);
    }

    public Task DeleteAsync(long id, CancellationToken ct = default) => _generic.DeleteAsync(id, ct);

    /// <summary>Validates the optional bookable-Tour link, mirroring Java's resolveTour() helper.</summary>
    private async Task ResolveTourAsync(TourProductDto dto, CancellationToken ct)
    {
        if (dto.TourId is { } tourId)
        {
            _ = await _tourRepository.GetByIdAsync(tourId, ct)
                ?? throw new ResourceNotFoundException($"Tour {tourId} was not found");
        }
    }
}
