using AutoMapper;
using eTour.Domain.Exceptions;

namespace eTour.Application.Common;

/// <summary>
/// Requirement #8: reusable generic CRUD implementation. Entity-specific services (e.g. TourService)
/// compose an instance of this rather than reimplementing plain CRUD.
/// </summary>
public class GenericService<TEntity, TDto, TKey> : IGenericService<TEntity, TDto, TKey> where TEntity : class
{
    protected readonly IGenericRepository<TEntity, TKey> Repository;
    protected readonly IMapper Mapper;

    public GenericService(IGenericRepository<TEntity, TKey> repository, IMapper mapper)
    {
        Repository = repository;
        Mapper = mapper;
    }

    public virtual async Task<TDto> GetByIdAsync(TKey id, CancellationToken ct = default)
    {
        var entity = await Repository.GetByIdAsync(id, ct) ?? throw NotFound(id);
        return Mapper.Map<TDto>(entity);
    }

    public virtual async Task<IReadOnlyList<TDto>> GetAllAsync(CancellationToken ct = default)
    {
        var entities = await Repository.GetAllAsync(ct);
        return Mapper.Map<IReadOnlyList<TDto>>(entities);
    }

    public virtual async Task<TDto> CreateAsync(TDto dto, CancellationToken ct = default)
    {
        var entity = Mapper.Map<TEntity>(dto);
        var saved = await Repository.AddAsync(entity, ct);
        return Mapper.Map<TDto>(saved);
    }

    public virtual async Task<TDto> UpdateAsync(TKey id, TDto dto, CancellationToken ct = default)
    {
        var entity = await Repository.GetByIdAsync(id, ct) ?? throw NotFound(id);
        Mapper.Map(dto, entity);
        // A DTO used for both create and update rarely carries a meaningful key of its own;
        // restore the real one in case the mapping just overwrote it with a default/stale value.
        Repository.SetKey(entity, id);
        await Repository.UpdateAsync(entity, ct);
        return Mapper.Map<TDto>(entity);
    }

    public virtual async Task DeleteAsync(TKey id, CancellationToken ct = default)
    {
        var entity = await Repository.GetByIdAsync(id, ct) ?? throw NotFound(id);
        await Repository.DeleteAsync(entity, ct);
    }

    private static ResourceNotFoundException NotFound(TKey id) =>
        new($"{typeof(TEntity).Name} with id '{id}' was not found");
}
