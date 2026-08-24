using eTour.Application.Dtos;
using eTour.Application.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace eTour.Api.Controllers;

/// <summary>BRD 3.6 Search: name, code, category, price range, duration and period, with pagination and sorting. Public - browsing tours doesn't require a login.</summary>
[ApiController]
[Route("api/tours/search")]
[AllowAnonymous]
public class TourSearchController : ControllerBase
{
    private readonly ITourSearchService _service;

    public TourSearchController(ITourSearchService service)
    {
        _service = service;
    }

    [HttpGet]
    public async Task<ActionResult<PagedResult<TourSearchResultDto>>> Search([FromQuery] TourSearchRequest request, CancellationToken ct) =>
        Ok(await _service.SearchAsync(request, ct));
}
