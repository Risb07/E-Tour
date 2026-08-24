using eTour.Application.Dtos;
using eTour.Application.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace eTour.Api.Controllers;

[ApiController]
[Route("api/wishlist")]
[Authorize(Roles = "CUSTOMER")]
public class WishlistController : ControllerBase
{
    private readonly IWishlistService _service;

    public WishlistController(IWishlistService service)
    {
        _service = service;
    }

    [HttpGet]
    public async Task<ActionResult<IReadOnlyList<WishlistItemResponse>>> GetMyWishlist(CancellationToken ct) =>
        Ok(await _service.GetMyWishlistAsync(ct));

    [HttpGet("tour/{tourId:long}")]
    public async Task<ActionResult<bool>> IsSaved(long tourId, CancellationToken ct) => Ok(await _service.IsSavedAsync(tourId, ct));

    [HttpPost("tour/{tourId:long}")]
    public async Task<ActionResult<WishlistItemResponse>> Add(long tourId, CancellationToken ct) =>
        StatusCode(StatusCodes.Status201Created, await _service.AddAsync(tourId, ct));

    [HttpDelete("tour/{tourId:long}")]
    public async Task<IActionResult> Remove(long tourId, CancellationToken ct)
    {
        await _service.RemoveAsync(tourId, ct);
        return NoContent();
    }
}
