using AutoMapper;
using eTour.Application.Common;
using eTour.Application.Dtos;
using eTour.Domain.Entities;

namespace eTour.Application.Services;

public class NavMenuService : INavMenuService
{
    private readonly IGenericService<NavMenuItem, NavMenuItemDto, long> _generic;
    private readonly IGenericRepository<NavMenuItem, long> _repository;
    private readonly IMapper _mapper;

    public NavMenuService(IGenericService<NavMenuItem, NavMenuItemDto, long> generic,
        IGenericRepository<NavMenuItem, long> repository, IMapper mapper)
    {
        _generic = generic;
        _repository = repository;
        _mapper = mapper;
    }

    public Task<IReadOnlyList<NavMenuItemDto>> GetTreeAsync(CancellationToken ct = default)
    {
        var all = _repository.Query().OrderBy(i => i.SortOrder).ToList();
        var dtos = all.ToDictionary(i => i.NavMenuItemId, i => _mapper.Map<NavMenuItemDto>(i));
        foreach (var dto in dtos.Values)
        {
            dto.Children.Clear();
        }

        var roots = new List<NavMenuItemDto>();
        foreach (var item in all)
        {
            var dto = dtos[item.NavMenuItemId];
            if (item.ParentItemId is { } parentId && dtos.TryGetValue(parentId, out var parentDto))
            {
                parentDto.Children.Add(dto);
            }
            else
            {
                roots.Add(dto);
            }
        }

        return Task.FromResult<IReadOnlyList<NavMenuItemDto>>(roots);
    }

    public Task<NavMenuItemDto> GetByIdAsync(long id, CancellationToken ct = default) => _generic.GetByIdAsync(id, ct);
    public Task<NavMenuItemDto> CreateAsync(NavMenuItemDto dto, CancellationToken ct = default) => _generic.CreateAsync(dto, ct);
    public Task<NavMenuItemDto> UpdateAsync(long id, NavMenuItemDto dto, CancellationToken ct = default) => _generic.UpdateAsync(id, dto, ct);
    public Task DeleteAsync(long id, CancellationToken ct = default) => _generic.DeleteAsync(id, ct);
}
