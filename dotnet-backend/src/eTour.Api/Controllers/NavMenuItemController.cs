using eTour.Application.Dtos;
using eTour.Application.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace eTour.Api.Controllers;

[ApiController]
[Route("api/nav-menu")]
public class NavMenuItemController : ControllerBase
{
    private readonly INavMenuService _service;

    public NavMenuItemController(INavMenuService service)
    {
        _service = service;
    }

    [HttpGet]
    [AllowAnonymous]
    public async Task<ActionResult<IReadOnlyList<NavMenuItemDto>>> GetTree(CancellationToken ct) => Ok(await _service.GetTreeAsync(ct));

    [HttpGet("{id:long}")]
    [AllowAnonymous]
    public async Task<ActionResult<NavMenuItemDto>> GetById(long id, CancellationToken ct) => Ok(await _service.GetByIdAsync(id, ct));

    [HttpPost]
    [Authorize(Roles = "ADMIN")]
    public async Task<ActionResult<NavMenuItemDto>> Create([FromBody] NavMenuItemDto dto, CancellationToken ct) =>
        StatusCode(StatusCodes.Status201Created, await _service.CreateAsync(dto, ct));

    [HttpPut("{id:long}")]
    [Authorize(Roles = "ADMIN")]
    public async Task<ActionResult<NavMenuItemDto>> Update(long id, [FromBody] NavMenuItemDto dto, CancellationToken ct) =>
        Ok(await _service.UpdateAsync(id, dto, ct));

    [HttpDelete("{id:long}")]
    [Authorize(Roles = "ADMIN")]
    public async Task<IActionResult> Delete(long id, CancellationToken ct)
    {
        await _service.DeleteAsync(id, ct);
        return NoContent();
    }
}
