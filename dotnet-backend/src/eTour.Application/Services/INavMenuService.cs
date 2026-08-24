using eTour.Application.Dtos;

namespace eTour.Application.Services;

public interface INavMenuService
{
    /// <summary>Builds the parent/child tree in-memory, matching Java's NavMenuItemController.</summary>
    Task<IReadOnlyList<NavMenuItemDto>> GetTreeAsync(CancellationToken ct = default);
    Task<NavMenuItemDto> GetByIdAsync(long id, CancellationToken ct = default);
    Task<NavMenuItemDto> CreateAsync(NavMenuItemDto dto, CancellationToken ct = default);
    Task<NavMenuItemDto> UpdateAsync(long id, NavMenuItemDto dto, CancellationToken ct = default);
    Task DeleteAsync(long id, CancellationToken ct = default);
}
