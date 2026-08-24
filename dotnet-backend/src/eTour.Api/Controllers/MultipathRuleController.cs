using eTour.Application.Dtos;
using eTour.Application.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace eTour.Api.Controllers;

/// <summary>Multipath rules: automatically place tours onto extra navigation paths. Entirely ADMIN-only - these rules change what appears where on the public site.</summary>
[ApiController]
[Route("api/multipath-rules")]
[Authorize(Roles = "ADMIN")]
public class MultipathRuleController : ControllerBase
{
    private readonly IMultipathRuleService _ruleService;
    private readonly IMultipathGeneratorService _generatorService;

    public MultipathRuleController(IMultipathRuleService ruleService, IMultipathGeneratorService generatorService)
    {
        _ruleService = ruleService;
        _generatorService = generatorService;
    }

    [HttpGet]
    public async Task<ActionResult<IReadOnlyList<TourTagRuleDto>>> List(CancellationToken ct) => Ok(await _ruleService.GetAllAsync(ct));

    [HttpPost]
    public async Task<ActionResult<TourTagRuleDto>> Create([FromBody] TourTagRuleDto dto, CancellationToken ct) =>
        StatusCode(StatusCodes.Status201Created, await _ruleService.CreateAsync(dto, ct));

    [HttpPut("{ruleId:long}")]
    public async Task<ActionResult<TourTagRuleDto>> Update(long ruleId, [FromBody] TourTagRuleDto dto, CancellationToken ct) =>
        Ok(await _ruleService.UpdateAsync(ruleId, dto, ct));

    [HttpDelete("{ruleId:long}")]
    public async Task<IActionResult> Delete(long ruleId, CancellationToken ct)
    {
        await _ruleService.DeleteAsync(ruleId, ct);
        return NoContent();
    }

    /// <summary>Dry run. Returns exactly what Apply() would change, writing nothing.</summary>
    [HttpPost("preview")]
    public async Task<ActionResult<MultipathPreviewResponse>> Preview(CancellationToken ct) => Ok(await _generatorService.PreviewAsync(ct));

    /// <summary>Applies every active rule. Safe to re-run - existing links are skipped.</summary>
    [HttpPost("apply")]
    public async Task<ActionResult<MultipathPreviewResponse>> Apply(CancellationToken ct) => Ok(await _generatorService.ApplyAsync(ct));
}
