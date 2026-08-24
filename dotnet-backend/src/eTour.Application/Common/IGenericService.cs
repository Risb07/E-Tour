namespace eTour.Application.Common;

/// <summary>Generic CRUD service contract (requirement #7) - DTO-facing, wraps <see cref="IGenericRepository{TEntity,TKey}"/>.</summary>
public interface IGenericService<TEntity, TDto, in TKey> where TEntity : class
{
    Task<TDto> GetByIdAsync(TKey id, CancellationToken ct = default);
    Task<IReadOnlyList<TDto>> GetAllAsync(CancellationToken ct = default);
    Task<TDto> CreateAsync(TDto dto, CancellationToken ct = default);
    Task<TDto> UpdateAsync(TKey id, TDto dto, CancellationToken ct = default);
    Task DeleteAsync(TKey id, CancellationToken ct = default);
}
