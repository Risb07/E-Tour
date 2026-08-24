using eTour.Application.Dtos;
using eTour.Application.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace eTour.Api.Controllers;

[ApiController]
[Route("api/cart")]
[Authorize(Roles = "ADMIN,CUSTOMER")]
public class CartController : ControllerBase
{
    private readonly ICartService _service;

    public CartController(ICartService service)
    {
        _service = service;
    }

    [HttpPost]
    public async Task<ActionResult<CartResponse>> AddToCart([FromBody] CartRequest request, CancellationToken ct) =>
        StatusCode(StatusCodes.Status201Created, await _service.AddToCartAsync(request, ct));

    [HttpGet]
    public async Task<ActionResult<IReadOnlyList<CartResponse>>> GetMyCart(CancellationToken ct) => Ok(await _service.GetMyCartAsync(ct));

    [HttpDelete("{cartId:long}")]
    public async Task<IActionResult> RemoveFromCart(long cartId, CancellationToken ct)
    {
        await _service.RemoveFromCartAsync(cartId, ct);
        return NoContent();
    }

    [HttpPost("{cartId:long}/checkout")]
    public async Task<ActionResult<BookingResponse>> Checkout(long cartId, CancellationToken ct) =>
        StatusCode(StatusCodes.Status201Created, await _service.CheckoutAsync(cartId, ct));
}
