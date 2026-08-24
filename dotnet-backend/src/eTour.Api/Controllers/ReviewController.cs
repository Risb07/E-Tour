using eTour.Application.Dtos;
using eTour.Application.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace eTour.Api.Controllers;

[ApiController]
[Route("api/reviews")]
public class ReviewController : ControllerBase
{
    private readonly IReviewService _service;

    public ReviewController(IReviewService service)
    {
        _service = service;
    }

    [HttpGet]
    [AllowAnonymous]
    public async Task<ActionResult<IReadOnlyList<ReviewResponse>>> GetAll(CancellationToken ct) => Ok(await _service.GetAllAsync(ct));

    [HttpGet("{id:long}")]
    [AllowAnonymous]
    public async Task<ActionResult<ReviewResponse>> GetById(long id, CancellationToken ct) => Ok(await _service.GetByIdAsync(id, ct));

    // ----- Moderation endpoints (ADMIN only) - act on any review by id. -----
    [HttpPost]
    [Authorize(Roles = "ADMIN")]
    public async Task<ActionResult<ReviewResponse>> Add([FromBody] ReviewResponse review, CancellationToken ct) =>
        StatusCode(StatusCodes.Status201Created, await _service.AddReviewAsync(review, ct));

    [HttpPut("{id:long}")]
    [Authorize(Roles = "ADMIN")]
    public async Task<ActionResult<ReviewResponse>> Edit(long id, [FromBody] ReviewRequest request, CancellationToken ct) =>
        Ok(await _service.EditReviewAsync(id, request, ct));

    [HttpDelete("{id:long}")]
    [Authorize(Roles = "ADMIN")]
    public async Task<IActionResult> Delete(long id, CancellationToken ct)
    {
        await _service.DeleteReviewAsync(id, ct);
        return NoContent();
    }

    [HttpGet("customer/{id:long}")]
    [Authorize(Roles = "ADMIN")]
    public async Task<ActionResult<IReadOnlyList<ReviewResponse>>> GetByCustomerId(long id, CancellationToken ct) =>
        Ok(await _service.GetByCustomerIdAsync(id, ct));

    [HttpGet("tour/{id:long}")]
    [AllowAnonymous]
    public async Task<ActionResult<IReadOnlyList<ReviewResponse>>> GetByTourId(long id, CancellationToken ct) =>
        Ok(await _service.GetByTourIdAsync(id, ct));

    [HttpGet("rating/{id:int}")]
    [AllowAnonymous]
    public async Task<ActionResult<IReadOnlyList<ReviewResponse>>> GetByRating(int id, CancellationToken ct) =>
        Ok(await _service.GetByRatingAsync(id, ct));

    [HttpGet("rating/sort")]
    [AllowAnonymous]
    public async Task<ActionResult<IReadOnlyList<ReviewResponse>>> SortByRating(CancellationToken ct) => Ok(await _service.SortByRatingAsync(ct));

    [HttpGet("tour/{id:long}/top5")]
    [AllowAnonymous]
    public async Task<ActionResult<IReadOnlyList<ReviewResponse>>> Top5Reviews(long id, CancellationToken ct) =>
        Ok(await _service.Top5ReviewsAsync(id, ct));

    [HttpGet("tour/{id:long}/avg")]
    [AllowAnonymous]
    public async Task<ActionResult<double>> GetAverageRating(long id, CancellationToken ct) => Ok(await _service.GetAverageRatingAsync(id, ct));

    // ----- Own-review endpoints - identity always from the JWT principal. -----
    [HttpPost("me/tour/{tourId:long}")]
    [Authorize(Roles = "CUSTOMER")]
    public async Task<ActionResult<ReviewResponse>> AddMyReview(long tourId, [FromBody] ReviewRequest request, CancellationToken ct) =>
        Ok(await _service.AddMyReviewAsync(tourId, request, ct));

    [HttpPut("me/tour/{tourId:long}")]
    [Authorize(Roles = "CUSTOMER")]
    public async Task<ActionResult<ReviewResponse>> EditMyReview(long tourId, [FromBody] ReviewRequest request, CancellationToken ct) =>
        Ok(await _service.EditMyReviewAsync(tourId, request, ct));

    [HttpDelete("me/{reviewId:long}")]
    [Authorize(Roles = "ADMIN,CUSTOMER")]
    public async Task<IActionResult> DeleteMyReview(long reviewId, CancellationToken ct)
    {
        await _service.DeleteMyReviewAsync(reviewId, ct);
        return NoContent();
    }
}
