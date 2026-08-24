using eTour.Application.Common;
using eTour.Application.Dtos;
using eTour.Domain.Entities;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace eTour.Api.Controllers;

[ApiController]
[Route("api/content")]
public class ContentController : ControllerBase
{
    private readonly IGenericService<Content, ContentDto, long> _service;
    private readonly IGenericRepository<Content, long> _repository;

    public ContentController(IGenericService<Content, ContentDto, long> service, IGenericRepository<Content, long> repository)
    {
        _service = service;
        _repository = repository;
    }

    [HttpGet]
    [AllowAnonymous]
    public async Task<ActionResult<IReadOnlyList<ContentDto>>> GetAll(CancellationToken ct) => Ok(await _service.GetAllAsync(ct));

    [HttpGet("{key}")]
    [AllowAnonymous]
    public ActionResult<ContentDto> GetByKey(string key, [FromQuery] string languageCode = "en")
    {
        var content = _repository.Query().FirstOrDefault(c => c.ContentKey == key && c.LanguageCode == languageCode);
        if (content is null)
        {
            return NotFound();
        }
        return Ok(new ContentDto
        {
            ContentId = content.ContentId,
            ContentKey = content.ContentKey,
            PageName = content.PageName,
            LanguageCode = content.LanguageCode,
            ContentValue = content.ContentValue,
            MediaUrl = content.MediaUrl,
            LinkUrl = content.LinkUrl,
            DisplayOrder = content.DisplayOrder,
            Status = content.Status
        });
    }

    [HttpPost]
    [Authorize(Roles = "ADMIN")]
    public async Task<ActionResult<ContentDto>> Create([FromBody] ContentDto dto, CancellationToken ct) =>
        StatusCode(StatusCodes.Status201Created, await _service.CreateAsync(dto, ct));

    [HttpPut("{id:long}")]
    [Authorize(Roles = "ADMIN")]
    public async Task<ActionResult<ContentDto>> Update(long id, [FromBody] ContentDto dto, CancellationToken ct) =>
        Ok(await _service.UpdateAsync(id, dto, ct));

    [HttpDelete("{id:long}")]
    [Authorize(Roles = "ADMIN")]
    public async Task<IActionResult> Delete(long id, CancellationToken ct)
    {
        await _service.DeleteAsync(id, ct);
        return NoContent();
    }
}
