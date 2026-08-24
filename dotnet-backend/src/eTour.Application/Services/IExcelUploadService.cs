using eTour.Application.Dtos;

namespace eTour.Application.Services;

public interface IExcelUploadService
{
    Task<ExcelUploadResult> UploadAsync(Stream fileStream, string? originalFileName, CancellationToken ct = default);
}

public interface IExcelTemplateService
{
    byte[] Generate();
}
