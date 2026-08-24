using eTour.Application.Dtos;
using eTour.Application.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace eTour.Api.Controllers;

[ApiController]
[Route("api/sub-sectors")]
public class SubSectorController : ControllerBase
{
    private readonly ISubSectorService _service;

    public SubSectorController(ISubSectorService service)
    {
        _service = service;
    }

    [HttpGet]
    [AllowAnonymous]
    public async Task<ActionResult<IReadOnlyList<SubSectorDto>>> GetAll(CancellationToken ct) => Ok(await _service.GetAllAsync(ct));

    [HttpGet("{id:long}")]
    [AllowAnonymous]
    public async Task<ActionResult<SubSectorDto>> GetById(long id, CancellationToken ct) => Ok(await _service.GetByIdAsync(id, ct));

    [HttpGet("{id:long}/products")]
    [AllowAnonymous]
    public async Task<ActionResult<IReadOnlyList<TourProductDto>>> GetProducts(long id, CancellationToken ct) =>
        Ok(await _service.GetProductsAsync(id, ct));

    [HttpPost]
    [Authorize(Roles = "ADMIN")]
    public async Task<ActionResult<SubSectorDto>> Create([FromBody] SubSectorDto dto, CancellationToken ct) =>
        StatusCode(StatusCodes.Status201Created, await _service.CreateAsync(dto, ct));

    [HttpPut("{id:long}")]
    [Authorize(Roles = "ADMIN")]
    public async Task<ActionResult<SubSectorDto>> Update(long id, [FromBody] SubSectorDto dto, CancellationToken ct) =>
        Ok(await _service.UpdateAsync(id, dto, ct));

    [HttpDelete("{id:long}")]
    [Authorize(Roles = "ADMIN")]
    public async Task<IActionResult> Delete(long id, CancellationToken ct)
    {
        await _service.DeleteAsync(id, ct);
        return NoContent();
    }
}
