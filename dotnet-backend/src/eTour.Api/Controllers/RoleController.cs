using eTour.Application.Common;
using eTour.Application.Dtos;
using eTour.Domain.Entities;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace eTour.Api.Controllers;

[ApiController]
[Route("api/roles")]
[Authorize(Roles = "ADMIN")]
public class RoleController : ControllerBase
{
    private readonly IGenericService<Role, RoleDto, long> _service;

    public RoleController(IGenericService<Role, RoleDto, long> service)
    {
        _service = service;
    }

    [HttpGet]
    public async Task<ActionResult<IReadOnlyList<RoleDto>>> GetAll(CancellationToken ct) => Ok(await _service.GetAllAsync(ct));

    [HttpPost]
    public async Task<ActionResult<RoleDto>> Create([FromBody] RoleDto dto, CancellationToken ct) =>
        StatusCode(StatusCodes.Status201Created, await _service.CreateAsync(dto, ct));
}
