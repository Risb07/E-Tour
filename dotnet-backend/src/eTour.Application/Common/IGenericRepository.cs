using System.Linq.Expressions;

namespace eTour.Application.Common;

/// <summary>Generic CRUD data-access contract (requirement #7), implemented once in Infrastructure over EF Core.</summary>
public interface IGenericRepository<TEntity, in TKey> where TEntity : class
{
    Task<TEntity?> GetByIdAsync(TKey id, CancellationToken ct = default);
    Task<IReadOnlyList<TEntity>> GetAllAsync(CancellationToken ct = default);
    Task<TEntity> AddAsync(TEntity entity, CancellationToken ct = default);
    Task UpdateAsync(TEntity entity, CancellationToken ct = default);
    Task DeleteAsync(TEntity entity, CancellationToken ct = default);
    Task<bool> ExistsAsync(TKey id, CancellationToken ct = default);

    /// <summary>Escape hatch for entity-specific services that need to compose their own LINQ queries.</summary>
    IQueryable<TEntity> Query();

    /// <summary>
    /// Forces the entity's primary key CLR property to <paramref name="id"/>. GenericService calls
    /// this right after mapping an update DTO onto a tracked entity: if the DTO's own key field
    /// was left at its default (typical for a payload that only carries mutable fields), the
    /// mapping would otherwise corrupt the entity's real key before it ever reaches UpdateAsync.
    /// </summary>
    void SetKey(TEntity entity, TKey id);

    /// <summary>
    /// Loads a not-yet-loaded collection navigation (e.g. a many-to-many skip navigation) onto an
    /// already-tracked entity. Required before reassigning that navigation to a new list: if EF
    /// never learns the "before" set of related entities, SaveChanges can't compute which join rows
    /// to delete, so removed associations silently linger and re-adding an existing one throws a
    /// duplicate-key violation on the join table's primary key.
    /// </summary>
    Task LoadCollectionAsync<TProperty>(TEntity entity, Expression<Func<TEntity, IEnumerable<TProperty>>> navigation,
        CancellationToken ct = default) where TProperty : class;
}
