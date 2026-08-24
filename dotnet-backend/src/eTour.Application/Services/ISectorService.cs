using eTour.Application.Dtos;

namespace eTour.Application.Services;

public interface ISectorService
{
    Task<IReadOnlyList<SectorDto>> GetAllAsync(CancellationToken ct = default);
    Task<SectorDto> GetByIdAsync(long id, CancellationToken ct = default);
    Task<IReadOnlyList<SubSectorDto>> GetSubSectorsAsync(long sectorId, CancellationToken ct = default);
    Task<IReadOnlyList<TourProductDto>> GetProductsAsync(long sectorId, CancellationToken ct = default);
    Task<SectorDto> CreateAsync(SectorDto dto, CancellationToken ct = default);
    Task<SectorDto> UpdateAsync(long id, SectorDto dto, CancellationToken ct = default);
    Task DeleteAsync(long id, CancellationToken ct = default);
}

public interface ISubSectorService
{
    Task<IReadOnlyList<SubSectorDto>> GetAllAsync(CancellationToken ct = default);
    Task<SubSectorDto> GetByIdAsync(long id, CancellationToken ct = default);
    Task<IReadOnlyList<TourProductDto>> GetProductsAsync(long subSectorId, CancellationToken ct = default);
    Task<SubSectorDto> CreateAsync(SubSectorDto dto, CancellationToken ct = default);
    Task<SubSectorDto> UpdateAsync(long id, SubSectorDto dto, CancellationToken ct = default);
    Task DeleteAsync(long id, CancellationToken ct = default);
}

public interface ITourProductService
{
    Task<IReadOnlyList<TourProductDto>> GetAllAsync(CancellationToken ct = default);
    Task<TourProductDto> GetByIdAsync(long id, CancellationToken ct = default);
    Task<TourProductDto> CreateAsync(TourProductDto dto, CancellationToken ct = default);
    Task<TourProductDto> UpdateAsync(long id, TourProductDto dto, CancellationToken ct = default);
    Task DeleteAsync(long id, CancellationToken ct = default);
}
