using eTour.Application.Dtos;
using eTour.Application.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace eTour.Api.Controllers;

[ApiController]
[Route("api/newsletter")]
[Authorize(Roles = "ADMIN")]
public class NewsletterController : ControllerBase
{
    private readonly INewsletterService _service;

    public NewsletterController(INewsletterService service)
    {
        _service = service;
    }

    [HttpPost("subscribe")]
    [AllowAnonymous]
    public async Task<ActionResult<NewsletterResponse>> Subscribe([FromBody] NewsletterRequest request, CancellationToken ct) =>
        Ok(await _service.SubscribeAsync(request, ct));

    [HttpPost("unsubscribe")]
    [AllowAnonymous]
    public async Task<IActionResult> Unsubscribe([FromBody] NewsletterRequest request, CancellationToken ct)
    {
        await _service.UnsubscribeAsync(request.Email, ct);
        return NoContent();
    }

    [HttpGet]
    public async Task<ActionResult<IReadOnlyList<NewsletterResponse>>> GetAll(CancellationToken ct) => Ok(await _service.GetAllAsync(ct));

    [HttpGet("count")]
    public async Task<ActionResult<long>> Count(CancellationToken ct) => Ok(await _service.CountAsync(ct));

    [HttpDelete("{id:long}")]
    public async Task<IActionResult> Delete(long id, CancellationToken ct)
    {
        await _service.DeleteAsync(id, ct);
        return NoContent();
    }
}
