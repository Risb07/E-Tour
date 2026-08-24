using eTour.Application.Dtos;
using eTour.Application.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace eTour.Api.Controllers;

/// <summary>Everything on the "Tour Page" tabs (Journey, Stay &amp; Meals, content tabs, Gallery/Video/Map, Add-ons, Itinerary) lives under the owning tour's URL.</summary>
[ApiController]
[Route("api/tours/{tourId:long}")]
public class TourDetailController : ControllerBase
{
    private readonly ITourDetailService _service;

    public TourDetailController(ITourDetailService service)
    {
        _service = service;
    }

    // ----- Journey -----
    [HttpGet("journey")]
    [AllowAnonymous]
    public async Task<ActionResult<IReadOnlyList<JourneyDetailDto>>> GetJourney(long tourId, CancellationToken ct) =>
        Ok(await _service.GetJourneyAsync(tourId, ct));

    [HttpPost("journey")]
    [Authorize(Roles = "ADMIN")]
    public async Task<ActionResult<JourneyDetailDto>> AddJourneyLeg(long tourId, [FromBody] JourneyDetailDto dto, CancellationToken ct) =>
        StatusCode(StatusCodes.Status201Created, await _service.AddJourneyLegAsync(tourId, dto, ct));

    // ----- Stay & Meals -----
    [HttpGet("stay-meals")]
    [AllowAnonymous]
    public async Task<ActionResult<IReadOnlyList<StayMealDto>>> GetStayMeals(long tourId, CancellationToken ct) =>
        Ok(await _service.GetStayMealsAsync(tourId, ct));

    [HttpPost("stay-meals")]
    [Authorize(Roles = "ADMIN")]
    public async Task<ActionResult<StayMealDto>> AddStayMeal(long tourId, [FromBody] StayMealDto dto, CancellationToken ct) =>
        StatusCode(StatusCodes.Status201Created, await _service.AddStayMealAsync(tourId, dto, ct));

    [HttpDelete("stay-meals/{stayMealId:long}")]
    [Authorize(Roles = "ADMIN")]
    public async Task<IActionResult> DeleteStayMeal(long tourId, long stayMealId, CancellationToken ct)
    {
        await _service.DeleteStayMealAsync(tourId, stayMealId, ct);
        return NoContent();
    }

    // ----- Content tabs -----
    [HttpGet("content")]
    [AllowAnonymous]
    public async Task<ActionResult<IReadOnlyList<TourContentDto>>> GetContent(long tourId, CancellationToken ct) =>
        Ok(await _service.GetContentAsync(tourId, ct));

    [HttpPost("content")]
    [Authorize(Roles = "ADMIN")]
    public async Task<ActionResult<TourContentDto>> UpsertContent(long tourId, [FromBody] TourContentDto dto, CancellationToken ct) =>
        StatusCode(StatusCodes.Status201Created, await _service.UpsertContentAsync(tourId, dto, ct));

    [HttpDelete("content/{tourContentId:long}")]
    [Authorize(Roles = "ADMIN")]
    public async Task<IActionResult> DeleteContent(long tourId, long tourContentId, CancellationToken ct)
    {
        await _service.DeleteContentAsync(tourId, tourContentId, ct);
        return NoContent();
    }

    // ----- Media -----
    [HttpGet("media")]
    [AllowAnonymous]
    public async Task<ActionResult<IReadOnlyList<TourMediaDto>>> GetMedia(long tourId, CancellationToken ct) =>
        Ok(await _service.GetMediaAsync(tourId, ct));

    [HttpPost("media")]
    [Authorize(Roles = "ADMIN")]
    public async Task<ActionResult<TourMediaDto>> AddMedia(long tourId, [FromBody] TourMediaDto dto, CancellationToken ct) =>
        StatusCode(StatusCodes.Status201Created, await _service.AddMediaAsync(tourId, dto, ct));

    [HttpDelete("media/{mediaId:long}")]
    [Authorize(Roles = "ADMIN")]
    public async Task<IActionResult> DeleteMedia(long tourId, long mediaId, CancellationToken ct)
    {
        await _service.DeleteMediaAsync(tourId, mediaId, ct);
        return NoContent();
    }

    // ----- Add-ons -----
    [HttpGet("addons")]
    [AllowAnonymous]
    public async Task<ActionResult<IReadOnlyList<TourAddonDto>>> GetAddons(long tourId, CancellationToken ct) =>
        Ok(await _service.GetAddonsAsync(tourId, ct));

    [HttpPost("addons")]
    [Authorize(Roles = "ADMIN")]
    public async Task<ActionResult<TourAddonDto>> AddAddon(long tourId, [FromBody] TourAddonDto dto, CancellationToken ct) =>
        StatusCode(StatusCodes.Status201Created, await _service.AddAddonAsync(tourId, dto, ct));

    [HttpPut("addons/{addonId:long}")]
    [Authorize(Roles = "ADMIN")]
    public async Task<ActionResult<TourAddonDto>> UpdateAddon(long tourId, long addonId, [FromBody] TourAddonDto dto, CancellationToken ct) =>
        Ok(await _service.UpdateAddonAsync(tourId, addonId, dto, ct));

    [HttpDelete("addons/{addonId:long}")]
    [Authorize(Roles = "ADMIN")]
    public async Task<IActionResult> DeleteAddon(long tourId, long addonId, CancellationToken ct)
    {
        await _service.DeleteAddonAsync(tourId, addonId, ct);
        return NoContent();
    }

    // ----- Itinerary -----
    [HttpGet("itinerary")]
    [AllowAnonymous]
    public async Task<ActionResult<IReadOnlyList<ItineraryDto>>> GetItinerary(long tourId, CancellationToken ct) =>
        Ok(await _service.GetItineraryAsync(tourId, ct));

    [HttpPost("itinerary")]
    [Authorize(Roles = "ADMIN")]
    public async Task<ActionResult<ItineraryDto>> AddItineraryDay(long tourId, [FromBody] ItineraryDto dto, CancellationToken ct) =>
        StatusCode(StatusCodes.Status201Created, await _service.AddItineraryDayAsync(tourId, dto, ct));

    [HttpPut("itinerary/{itineraryId:long}")]
    [Authorize(Roles = "ADMIN")]
    public async Task<ActionResult<ItineraryDto>> UpdateItineraryDay(long tourId, long itineraryId, [FromBody] ItineraryDto dto, CancellationToken ct) =>
        Ok(await _service.UpdateItineraryDayAsync(tourId, itineraryId, dto, ct));

    [HttpDelete("itinerary/{itineraryId:long}")]
    [Authorize(Roles = "ADMIN")]
    public async Task<IActionResult> DeleteItineraryDay(long tourId, long itineraryId, CancellationToken ct)
    {
        await _service.DeleteItineraryDayAsync(tourId, itineraryId, ct);
        return NoContent();
    }
}
