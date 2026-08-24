using AutoMapper;
using AutoMapper.QueryableExtensions;
using eTour.Application.Common;
using eTour.Application.Dtos;
using eTour.Domain.Entities;
using eTour.Domain.Exceptions;

namespace eTour.Application.Services;

/// <summary>
/// Category has asymmetric request/response shapes (CategoryResponse carries a nested
/// ParentCategory and a computed ActiveTourCount), so it composes the generic REPOSITORY
/// directly rather than IGenericService&lt;T,TDto,TKey&gt; (which assumes one DTO shape for
/// both directions).
/// </summary>
public class CategoryService : ICategoryService
{
    private readonly IGenericRepository<Category, long> _repository;
    private readonly IMapper _mapper;

    public CategoryService(IGenericRepository<Category, long> repository, IMapper mapper)
    {
        _repository = repository;
        _mapper = mapper;
    }

    // Reads go through ProjectTo rather than the repository's plain entity fetch: the nested
    // ParentCategory and the ActiveTourCount subquery both need to be resolved in SQL, and
    // Query() never eager-loads navigations (a mapped entity would report a null parent).
    public Task<IReadOnlyList<CategoryResponse>> GetAllAsync(CancellationToken ct = default) =>
        Task.FromResult<IReadOnlyList<CategoryResponse>>(
            _repository.Query().ProjectTo<CategoryResponse>(_mapper.ConfigurationProvider).ToList());

    public Task<CategoryResponse> GetByIdAsync(long id, CancellationToken ct = default)
    {
        var response = _repository.Query().Where(c => c.CategoryId == id)
            .ProjectTo<CategoryResponse>(_mapper.ConfigurationProvider).FirstOrDefault();
        return response is null
            ? throw new ResourceNotFoundException($"Category {id} was not found")
            : Task.FromResult(response);
    }

    public async Task<CategoryResponse> CreateAsync(CategoryRequest request, CancellationToken ct = default)
    {
        var entity = _mapper.Map<Category>(request);
        var saved = await _repository.AddAsync(entity, ct);
        // Re-read so the response carries the same fully-resolved shape as a GET.
        return await GetByIdAsync(saved.CategoryId, ct);
    }

    public async Task<CategoryResponse> UpdateAsync(long id, CategoryRequest request, CancellationToken ct = default)
    {
        var entity = await RequireAsync(id, ct);
        _mapper.Map(request, entity);
        _repository.SetKey(entity, id);
        await _repository.UpdateAsync(entity, ct);
        return await GetByIdAsync(id, ct);
    }

    public async Task DeleteAsync(long id, CancellationToken ct = default)
    {
        var entity = await RequireAsync(id, ct);
        await _repository.DeleteAsync(entity, ct);
    }

    private async Task<Category> RequireAsync(long id, CancellationToken ct) =>
        await _repository.GetByIdAsync(id, ct) ?? throw new ResourceNotFoundException($"Category {id} was not found");
}
