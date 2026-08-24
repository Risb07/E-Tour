using eTour.Application.Dtos;
using eTour.Application.Services;
using eTour.Domain.Exceptions;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace eTour.Api.Controllers;

[ApiController]
[Route("api/excel-upload")]
[Authorize(Roles = "ADMIN")]
public class ExcelUploadController : ControllerBase
{
    private readonly IExcelUploadService _uploadService;
    private readonly IExcelTemplateService _templateService;

    public ExcelUploadController(IExcelUploadService uploadService, IExcelTemplateService templateService)
    {
        _uploadService = uploadService;
        _templateService = templateService;
    }

    /// <summary>Downloadable .xlsx template, generated from the same column contract the parser uses.</summary>
    [HttpGet("template")]
    public IActionResult DownloadTemplate()
    {
        var workbook = _templateService.Generate();
        return File(workbook, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "etour-tour-upload-template.xlsx");
    }

    [HttpPost]
    [Consumes("multipart/form-data")]
    public async Task<ActionResult<ExcelUploadResult>> Upload(IFormFile? file, CancellationToken ct)
    {
        if (file is null || file.Length == 0)
        {
            throw new IllegalOperationException("Upload file is empty");
        }

        await using var stream = file.OpenReadStream();
        return Ok(await _uploadService.UploadAsync(stream, file.FileName, ct));
    }
}
