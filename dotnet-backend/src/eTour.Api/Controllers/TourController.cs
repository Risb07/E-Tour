using eTour.Application.Dtos;
using eTour.Application.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace eTour.Api.Controllers;

[ApiController]
[Route("api/tours")]
public class TourController : ControllerBase
{
    private readonly ITourService _service;

    public TourController(ITourService service)
    {
        _service = service;
    }

    [HttpGet("test")]
    [AllowAnonymous]
    public ActionResult<string> Test() => Ok("Controller Working");

    [HttpGet("{id:long}")]
    [AllowAnonymous]
    public async Task<ActionResult<TourResponse>> GetById(long id, CancellationToken ct) => Ok(await _service.GetByIdAsync(id, ct));

    [HttpGet]
    [AllowAnonymous]
    public async Task<ActionResult<IReadOnlyList<TourResponse>>> GetAll(CancellationToken ct) => Ok(await _service.GetAllAsync(ct));

    [HttpGet("code/{code}")]
    [AllowAnonymous]
    public async Task<ActionResult<IReadOnlyList<TourResponse>>> GetByCode(string code, CancellationToken ct) =>
        Ok(await _service.GetByTourCodeAsync(code, ct));

    [HttpGet("{id:long}/details")]
    [AllowAnonymous]
    public async Task<ActionResult<TourDetailsResponse>> GetDetails(long id, CancellationToken ct) =>
        Ok(await _service.GetTourDetailsAsync(id, ct));

    [HttpPost]
    [Authorize(Roles = "ADMIN")]
    public async Task<ActionResult<TourResponse>> Create([FromBody] TourRequest request, CancellationToken ct) =>
        StatusCode(StatusCodes.Status201Created, await _service.CreateAsync(request, ct));

    [HttpPut("{id:long}")]
    [Authorize(Roles = "ADMIN")]
    public async Task<ActionResult<TourResponse>> Update(long id, [FromBody] TourRequest request, CancellationToken ct) =>
        Ok(await _service.UpdateAsync(id, request, ct));

    [HttpDelete("{id:long}")]
    [Authorize(Roles = "ADMIN")]
    public async Task<IActionResult> Delete(long id, CancellationToken ct)
    {
        await _service.DeleteAsync(id, ct);
        return NoContent();
    }
}
