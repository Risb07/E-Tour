using eTour.Application.Common;
using eTour.Application.Dtos;
using eTour.Domain.Entities;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace eTour.Api.Controllers;

[ApiController]
[Route("api/ad-banners")]
public class AdBannerController : ControllerBase
{
    private readonly IGenericService<AdBanner, AdBannerDto, long> _service;

    public AdBannerController(IGenericService<AdBanner, AdBannerDto, long> service)
    {
        _service = service;
    }

    [HttpGet]
    [AllowAnonymous]
    public async Task<ActionResult<IReadOnlyList<AdBannerDto>>> GetAll([FromQuery] string? position, [FromQuery] bool includeInactive,
        CancellationToken ct)
    {
        var all = await _service.GetAllAsync(ct);
        var query = all.AsEnumerable();
        if (!includeInactive)
        {
            query = query.Where(a => a.Status);
        }
        if (!string.IsNullOrWhiteSpace(position))
        {
            query = query.Where(a => string.Equals(a.Position, position, StringComparison.OrdinalIgnoreCase));
        }
        return Ok(query.ToList());
    }

    [HttpGet("{id:long}")]
    [AllowAnonymous]
    public async Task<ActionResult<AdBannerDto>> GetById(long id, CancellationToken ct) => Ok(await _service.GetByIdAsync(id, ct));

    [HttpPost]
    [Authorize(Roles = "ADMIN")]
    public async Task<ActionResult<AdBannerDto>> Create([FromBody] AdBannerDto dto, CancellationToken ct) =>
        StatusCode(StatusCodes.Status201Created, await _service.CreateAsync(dto, ct));

    [HttpPut("{id:long}")]
    [Authorize(Roles = "ADMIN")]
    public async Task<ActionResult<AdBannerDto>> Update(long id, [FromBody] AdBannerDto dto, CancellationToken ct) =>
        Ok(await _service.UpdateAsync(id, dto, ct));

    [HttpDelete("{id:long}")]
    [Authorize(Roles = "ADMIN")]
    public async Task<IActionResult> Delete(long id, CancellationToken ct)
    {
        await _service.DeleteAsync(id, ct);
        return NoContent();
    }
}
