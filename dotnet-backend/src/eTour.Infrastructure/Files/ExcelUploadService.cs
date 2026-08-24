using ClosedXML.Excel;
using eTour.Application.Common;
using eTour.Application.Dtos;
using eTour.Application.Services;
using eTour.Domain.Entities;
using eTour.Domain.Enums;
using eTour.Domain.Exceptions;

namespace eTour.Infrastructure.Files;

/// <summary>
/// BRD Module 16 - Excel Upload. Expected columns (row 1 = header, data from row 2): title |
/// description | durationDays | basePrice | tourCode (ADV/INT/DEV/DOM) | categoryName | status
/// (ACTIVE/INACTIVE/DRAFT). Duplicate detection is by exact tour title (case-insensitive)
/// against existing tours AND within the same file. Every row failure is collected into the
/// response rather than aborting the whole batch.
/// </summary>
public class ExcelUploadService : IExcelUploadService
{
    private readonly IGenericRepository<Tour, long> _tourRepository;
    private readonly IGenericRepository<Category, long> _categoryRepository;
    private readonly IGenericRepository<ExcelUploadBatch, long> _batchRepository;
    private readonly ICurrentUserService _currentUser;
    private readonly IFileStorageService _fileStorageService;

    public ExcelUploadService(IGenericRepository<Tour, long> tourRepository, IGenericRepository<Category, long> categoryRepository,
        IGenericRepository<ExcelUploadBatch, long> batchRepository, ICurrentUserService currentUser, IFileStorageService fileStorageService)
    {
        _tourRepository = tourRepository;
        _categoryRepository = categoryRepository;
        _batchRepository = batchRepository;
        _currentUser = currentUser;
        _fileStorageService = fileStorageService;
    }

    public async Task<ExcelUploadResult> UploadAsync(Stream fileStream, string? originalFileName, CancellationToken ct = default)
    {
        if (fileStream.Length == 0)
        {
            throw new IllegalOperationException("Upload file is empty");
        }

        var user = await _currentUser.CurrentUserAsync(ct);

        // Store a copy on disk before parsing - the stream is consumed by ClosedXML below, so
        // seek back to the start afterward.
        var storedPath = await _fileStorageService.StoreAsync(fileStream, originalFileName ?? "upload.xlsx", "excel-batches", ct);
        fileStream.Position = 0;

        var batch = new ExcelUploadBatch
        {
            UploadedById = user.UserId,
            FileName = originalFileName ?? "upload.xlsx",
            FilePath = storedPath,
            Status = UploadBatchStatus.PROCESSING,
            UploadedAt = DateTime.UtcNow
        };
        batch = await _batchRepository.AddAsync(batch, ct);

        var rowErrors = new List<string>();
        var total = 0;
        var success = 0;
        var seenTitles = new List<string>();

        try
        {
            using var workbook = new XLWorkbook(fileStream);
            var sheet = workbook.Worksheet(1);
            var lastRow = sheet.LastRowUsed()?.RowNumber() ?? 1;

            for (var rowIdx = 2; rowIdx <= lastRow; rowIdx++)
            {
                var row = sheet.Row(rowIdx);
                if (IsRowBlank(row))
                {
                    continue;
                }
                total++;

                try
                {
                    var title = GetString(row, 1);
                    var description = GetString(row, 2);
                    var durationDays = (int)GetNumeric(row, 3);
                    var basePrice = (decimal)GetNumeric(row, 4);
                    var tourCodeStr = GetString(row, 5);
                    var categoryName = GetString(row, 6);
                    var statusStr = GetString(row, 7);

                    if (string.IsNullOrWhiteSpace(title))
                    {
                        throw new ArgumentException("title is required");
                    }
                    if (string.IsNullOrWhiteSpace(tourCodeStr))
                    {
                        throw new ArgumentException("tourCode is required");
                    }
                    if (basePrice <= 0)
                    {
                        throw new ArgumentException("basePrice must be greater than zero");
                    }
                    if (durationDays < 1)
                    {
                        throw new ArgumentException("durationDays must be at least 1");
                    }

                    // Duplicate detection: existing DB rows AND earlier rows in this same file.
                    if (_tourRepository.Query().Any(t => t.Title.ToLower() == title.ToLower())
                        || seenTitles.Any(t => string.Equals(t, title, StringComparison.OrdinalIgnoreCase)))
                    {
                        throw new ArgumentException($"duplicate tour title '{title}'");
                    }

                    if (!Enum.TryParse<TourCode>(tourCodeStr.Trim(), true, out var tourCode))
                    {
                        throw new ArgumentException($"invalid tourCode '{tourCodeStr}'");
                    }

                    var status = TourStatus.DRAFT;
                    if (!string.IsNullOrWhiteSpace(statusStr) && !Enum.TryParse(statusStr.Trim(), true, out status))
                    {
                        throw new ArgumentException($"invalid status '{statusStr}'");
                    }

                    var tour = new Tour
                    {
                        Title = title,
                        Description = description,
                        DurationDays = durationDays,
                        BasePrice = basePrice,
                        TourCode = tourCode,
                        Status = status
                    };

                    if (!string.IsNullOrWhiteSpace(categoryName))
                    {
                        var category = _categoryRepository.Query().FirstOrDefault(c => c.CategoryName == categoryName.Trim())
                            ?? throw new ArgumentException($"category '{categoryName}' does not exist");
                        tour.Categories.Add(category);
                    }

                    await _tourRepository.AddAsync(tour, ct);
                    seenTitles.Add(title);
                    success++;
                }
                catch (Exception rowEx)
                {
                    rowErrors.Add($"Row {rowIdx}: {rowEx.Message}");
                }
            }
        }
        catch (Exception ex) when (ex is not IllegalOperationException)
        {
            batch.Status = UploadBatchStatus.FAILED;
            batch.TotalRows = total;
            batch.SuccessRows = success;
            batch.FailedRows = total - success;
            await _batchRepository.UpdateAsync(batch, ct);
            throw new IllegalOperationException($"Could not read the Excel file: {ex.Message}");
        }

        var failed = total - success;
        batch.TotalRows = total;
        batch.SuccessRows = success;
        batch.FailedRows = failed;
        batch.Status = failed == 0 ? UploadBatchStatus.COMPLETED : UploadBatchStatus.COMPLETED_WITH_ERRORS;
        await _batchRepository.UpdateAsync(batch, ct);

        return new ExcelUploadResult
        {
            BatchId = batch.BatchId,
            FileName = batch.FileName,
            TotalRows = total,
            SuccessRows = success,
            FailedRows = failed,
            Status = batch.Status.ToString(),
            Errors = rowErrors
        };
    }

    private static bool IsRowBlank(IXLRow row) => row.CellsUsed().All(c => c.IsEmpty());

    private static string? GetString(IXLRow row, int col)
    {
        var cell = row.Cell(col);
        var value = cell.IsEmpty() ? null : cell.GetFormattedString();
        return string.IsNullOrWhiteSpace(value) ? null : value.Trim();
    }

    private static double GetNumeric(IXLRow row, int col)
    {
        var cell = row.Cell(col);
        if (cell.IsEmpty())
        {
            return 0;
        }
        if (cell.DataType == XLDataType.Number)
        {
            return cell.GetDouble();
        }
        var text = cell.GetFormattedString();
        return !string.IsNullOrWhiteSpace(text) && double.TryParse(text.Trim(), out var parsed) ? parsed : 0;
    }
}
