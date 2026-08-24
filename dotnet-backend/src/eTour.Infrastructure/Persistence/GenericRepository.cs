using System.Linq.Expressions;
using eTour.Application.Common;
using Microsoft.EntityFrameworkCore;

namespace eTour.Infrastructure.Persistence;

public class GenericRepository<TEntity, TKey> : IGenericRepository<TEntity, TKey> where TEntity : class
{
    private readonly EtourDbContext _context;
    private readonly DbSet<TEntity> _set;

    public GenericRepository(EtourDbContext context)
    {
        _context = context;
        _set = context.Set<TEntity>();
    }

    public async Task<TEntity?> GetByIdAsync(TKey id, CancellationToken ct = default) =>
        await _set.FindAsync([id], ct);

    public async Task<IReadOnlyList<TEntity>> GetAllAsync(CancellationToken ct = default) =>
        await _set.AsNoTracking().ToListAsync(ct);

    public async Task<TEntity> AddAsync(TEntity entity, CancellationToken ct = default)
    {
        _set.Add(entity);
        await _context.SaveChangesAsync(ct);
        return entity;
    }

    public async Task UpdateAsync(TEntity entity, CancellationToken ct = default)
    {
        _set.Update(entity);
        await _context.SaveChangesAsync(ct);
    }

    public async Task DeleteAsync(TEntity entity, CancellationToken ct = default)
    {
        _set.Remove(entity);
        await _context.SaveChangesAsync(ct);
    }

    public async Task<bool> ExistsAsync(TKey id, CancellationToken ct = default) =>
        await GetByIdAsync(id, ct) is not null;

    public IQueryable<TEntity> Query() => _set.AsQueryable();

    public void SetKey(TEntity entity, TKey id)
    {
        // Uses static model metadata (not the change tracker), so this never triggers EF's
        // automatic change detection - safe to call on an already-tracked entity whose key a
        // mapper may have just overwritten with a stale/default value.
        var keyProperty = _context.Model.FindEntityType(typeof(TEntity))!.FindPrimaryKey()!.Properties.Single();
        keyProperty.PropertyInfo!.SetValue(entity, id);
    }

    public async Task LoadCollectionAsync<TProperty>(TEntity entity, Expression<Func<TEntity, IEnumerable<TProperty>>> navigation,
        CancellationToken ct = default) where TProperty : class
    {
        var entry = _context.Entry(entity).Collection(navigation);
        if (!entry.IsLoaded)
        {
            await entry.LoadAsync(ct);
        }
    }
}
