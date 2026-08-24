using eTour.Application.Dtos;
using eTour.Application.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace eTour.Api.Controllers;

[ApiController]
[Route("api/contact")]
[Authorize(Roles = "ADMIN")]
public class ContactController : ControllerBase
{
    private readonly IContactService _service;

    public ContactController(IContactService service)
    {
        _service = service;
    }

    [HttpPost]
    [AllowAnonymous]
    public async Task<ActionResult<ContactEnquiryResponse>> Submit([FromBody] ContactEnquiryRequest request, CancellationToken ct) =>
        StatusCode(StatusCodes.Status201Created, await _service.SubmitAsync(request, ct));

    [HttpGet]
    public async Task<ActionResult<IReadOnlyList<ContactEnquiryResponse>>> GetAll(CancellationToken ct) => Ok(await _service.GetAllAsync(ct));

    [HttpGet("{id:long}")]
    public async Task<ActionResult<ContactEnquiryResponse>> GetById(long id, CancellationToken ct) => Ok(await _service.GetByIdAsync(id, ct));

    [HttpPatch("{id:long}/status")]
    public async Task<ActionResult<ContactEnquiryResponse>> UpdateStatus(long id, [FromQuery] string status, CancellationToken ct) =>
        Ok(await _service.UpdateStatusAsync(id, status, ct));

    [HttpGet("count")]
    public async Task<ActionResult<long>> Count(CancellationToken ct) => Ok(await _service.CountAsync(ct));

    [HttpDelete("{id:long}")]
    public async Task<IActionResult> Delete(long id, CancellationToken ct)
    {
        await _service.DeleteAsync(id, ct);
        return NoContent();
    }
}
