using eTour.Application.Dtos;

namespace eTour.Application.Services;

public interface ICategoryService
{
    Task<IReadOnlyList<CategoryResponse>> GetAllAsync(CancellationToken ct = default);
    Task<CategoryResponse> GetByIdAsync(long id, CancellationToken ct = default);
    Task<CategoryResponse> CreateAsync(CategoryRequest request, CancellationToken ct = default);
    Task<CategoryResponse> UpdateAsync(long id, CategoryRequest request, CancellationToken ct = default);
    Task DeleteAsync(long id, CancellationToken ct = default);
}
