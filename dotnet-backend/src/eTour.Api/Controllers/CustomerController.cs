using eTour.Application.Dtos;
using eTour.Application.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace eTour.Api.Controllers;

[ApiController]
[Route("api/customer")]
public class CustomerController : ControllerBase
{
    private readonly ICustomerService _service;

    public CustomerController(ICustomerService service)
    {
        _service = service;
    }

    // Admin only: customers are normally created via /api/users/register, which also creates
    // the linked Customer row. This exists for admin back-office data entry.
    [HttpGet]
    [Authorize(Roles = "ADMIN")]
    public async Task<ActionResult<IReadOnlyList<CustomerResponse>>> GetAll(CancellationToken ct) => Ok(await _service.GetAllAsync(ct));

    [HttpPost]
    [Authorize(Roles = "ADMIN")]
    public async Task<ActionResult<CustomerResponse>> Create([FromBody] CustomerRequest request, CancellationToken ct) =>
        StatusCode(StatusCodes.Status201Created, await _service.CreateAsync(request, ct));

    // Accessible to the profile's own customer or an admin (ownership enforced inside the service).
    [HttpGet("{id:long}")]
    [Authorize(Roles = "ADMIN,CUSTOMER")]
    public async Task<ActionResult<CustomerResponse>> GetById(long id, CancellationToken ct) => Ok(await _service.GetByIdAsync(id, ct));

    [HttpPut("{id:long}")]
    [Authorize(Roles = "ADMIN,CUSTOMER")]
    public async Task<ActionResult<CustomerResponse>> Update(long id, [FromBody] CustomerRequest request, CancellationToken ct) =>
        Ok(await _service.UpdateAsync(id, request, ct));

    // Own-profile endpoints resolved from the JWT principal, not a path id.
    [HttpGet("me")]
    [Authorize(Roles = "CUSTOMER")]
    public async Task<ActionResult<CustomerResponse>> GetMyProfile(CancellationToken ct) => Ok(await _service.GetCurrentAsync(ct));

    [HttpPut("me")]
    [Authorize(Roles = "CUSTOMER")]
    public async Task<ActionResult<CustomerResponse>> UpdateMyProfile([FromBody] UpdateProfileRequest request, CancellationToken ct) =>
        Ok(await _service.UpdateCurrentAsync(request, ct));

    [HttpDelete("{id:long}")]
    [Authorize(Roles = "ADMIN")]
    public async Task<IActionResult> Delete(long id, CancellationToken ct)
    {
        await _service.DeleteAsync(id, ct);
        return NoContent();
    }
}
