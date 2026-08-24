using eTour.Application.Dtos;
using eTour.Application.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace eTour.Api.Controllers;

[ApiController]
[Route("api/sectors")]
public class SectorController : ControllerBase
{
    private readonly ISectorService _service;

    public SectorController(ISectorService service)
    {
        _service = service;
    }

    [HttpGet]
    [AllowAnonymous]
    public async Task<ActionResult<IReadOnlyList<SectorDto>>> GetAll(CancellationToken ct) => Ok(await _service.GetAllAsync(ct));

    [HttpGet("{id:long}")]
    [AllowAnonymous]
    public async Task<ActionResult<SectorDto>> GetById(long id, CancellationToken ct) => Ok(await _service.GetByIdAsync(id, ct));

    [HttpGet("{id:long}/sub-sectors")]
    [AllowAnonymous]
    public async Task<ActionResult<IReadOnlyList<SubSectorDto>>> GetSubSectors(long id, CancellationToken ct) =>
        Ok(await _service.GetSubSectorsAsync(id, ct));

    [HttpGet("{id:long}/products")]
    [AllowAnonymous]
    public async Task<ActionResult<IReadOnlyList<TourProductDto>>> GetProducts(long id, CancellationToken ct) =>
        Ok(await _service.GetProductsAsync(id, ct));

    [HttpPost]
    [Authorize(Roles = "ADMIN")]
    public async Task<ActionResult<SectorDto>> Create([FromBody] SectorDto dto, CancellationToken ct) =>
        StatusCode(StatusCodes.Status201Created, await _service.CreateAsync(dto, ct));

    [HttpPut("{id:long}")]
    [Authorize(Roles = "ADMIN")]
    public async Task<ActionResult<SectorDto>> Update(long id, [FromBody] SectorDto dto, CancellationToken ct) =>
        Ok(await _service.UpdateAsync(id, dto, ct));

    [HttpDelete("{id:long}")]
    [Authorize(Roles = "ADMIN")]
    public async Task<IActionResult> Delete(long id, CancellationToken ct)
    {
        await _service.DeleteAsync(id, ct);
        return NoContent();
    }
}
