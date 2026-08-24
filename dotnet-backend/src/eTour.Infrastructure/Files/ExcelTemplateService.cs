using ClosedXML.Excel;
using eTour.Application.Services;
using eTour.Domain.Exceptions;

namespace eTour.Infrastructure.Files;

/// <summary>
/// Builds the bulk-upload .xlsx template. Column order here is the contract ExcelUploadService
/// parses, so the two share the Columns constant and must stay in step.
/// </summary>
public class ExcelTemplateService : IExcelTemplateService
{
    /// <summary>Must match the order ExcelUploadService reads.</summary>
    public static readonly string[] Columns = ["title", "description", "durationDays", "basePrice", "tourCode", "categoryName", "status"];

    private static readonly (string Column, string Rule)[] Notes =
    [
        ("title", "Required. Tour name, max 200 characters."),
        ("description", "Optional. Free text."),
        ("durationDays", "Required. Whole number, at least 1."),
        ("basePrice", "Required. Number greater than 0. Digits only - no currency symbol or commas."),
        ("tourCode", "Required. One of: ADV, INT, DEV, DOM."),
        ("categoryName", "Optional. Must match an existing category name exactly."),
        ("status", "Optional. ACTIVE or DRAFT. Defaults to DRAFT.")
    ];

    public byte[] Generate()
    {
        try
        {
            using var workbook = new XLWorkbook();

            // Sheet 1: the actual upload sheet - header row plus one example.
            var sheet = workbook.Worksheets.Add("Tours");
            for (var i = 0; i < Columns.Length; i++)
            {
                var cell = sheet.Cell(1, i + 1);
                cell.Value = Columns[i];
                cell.Style.Font.Bold = true;
                cell.Style.Fill.BackgroundColor = XLColor.FromHtml("#D9D9D9");
            }

            var exampleValues = new object[] { "Kerala Backwaters", "Houseboat stay and spice plantation tour", 6, 24000, "DOM", "Domestic", "ACTIVE" };
            for (var i = 0; i < exampleValues.Length; i++)
            {
                sheet.Cell(2, i + 1).Value = XLCellValue.FromObject(exampleValues[i]);
            }

            sheet.Columns().AdjustToContents();

            // Sheet 2: validation rules, so the instructions travel with the file.
            var help = workbook.Worksheets.Add("Instructions");
            help.Cell(1, 1).Value = "Column";
            help.Cell(1, 1).Style.Font.Bold = true;
            help.Cell(1, 2).Value = "Rule";
            help.Cell(1, 2).Style.Font.Bold = true;

            for (var i = 0; i < Notes.Length; i++)
            {
                help.Cell(i + 2, 1).Value = Notes[i].Column;
                help.Cell(i + 2, 2).Value = Notes[i].Rule;
            }

            help.Cell(Notes.Length + 3, 1).Value = "Note";
            help.Cell(Notes.Length + 3, 2).Value =
                "Delete the example row before uploading. Row 1 must stay as the header. " +
                "Invalid rows are reported individually and skipped - valid rows still import.";

            help.Columns().AdjustToContents();

            using var stream = new MemoryStream();
            workbook.SaveAs(stream);
            return stream.ToArray();
        }
        catch (Exception ex)
        {
            throw new IllegalOperationException($"Could not generate the Excel template: {ex.Message}");
        }
    }
}
