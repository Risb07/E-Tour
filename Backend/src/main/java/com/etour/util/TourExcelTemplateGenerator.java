package com.etour.util;

import java.io.ByteArrayOutputStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import com.etour.exception.IllegalOperationException;

/**
 * Builds the bulk-upload .xlsx template. Column order here is the contract
 * ExcelUploadServiceImpl parses, so the two must stay in step - generating the
 * template from code rather than shipping a static file is what keeps them
 * from drifting apart.
 */
@Component
public class TourExcelTemplateGenerator {

    /** Must match the order ExcelUploadServiceImpl reads. */
    public static final String[] COLUMNS = {
            "title", "description", "durationDays", "basePrice", "tourCode", "categoryName", "status"
    };

    private static final String[][] NOTES = {
            { "title", "Required. Tour name, max 200 characters." },
            { "description", "Optional. Free text." },
            { "durationDays", "Required. Whole number, at least 1." },
            { "basePrice", "Required. Number greater than 0. Digits only - no currency symbol or commas." },
            { "tourCode", "Required. One of: ADV, INT, DEV, DOM." },
            { "categoryName", "Optional. Must match an existing category name exactly." },
            { "status", "Optional. ACTIVE or DRAFT. Defaults to DRAFT." },
    };

    public byte[] generate() {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Sheet 1: the actual upload sheet - header row plus one example.
            Sheet sheet = workbook.createSheet("Tours");
            Row header = sheet.createRow(0);
            for (int i = 0; i < COLUMNS.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(COLUMNS[i]);
                cell.setCellStyle(headerStyle);
            }

            Row example = sheet.createRow(1);
            example.createCell(0).setCellValue("Kerala Backwaters");
            example.createCell(1).setCellValue("Houseboat stay and spice plantation tour");
            example.createCell(2).setCellValue(6);
            example.createCell(3).setCellValue(24000);
            example.createCell(4).setCellValue("DOM");
            example.createCell(5).setCellValue("Domestic");
            example.createCell(6).setCellValue("ACTIVE");

            for (int i = 0; i < COLUMNS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Sheet 2: validation rules, so the instructions travel with the file.
            Sheet help = workbook.createSheet("Instructions");
            Row helpHeader = help.createRow(0);
            Cell c0 = helpHeader.createCell(0);
            c0.setCellValue("Column");
            c0.setCellStyle(headerStyle);
            Cell c1 = helpHeader.createCell(1);
            c1.setCellValue("Rule");
            c1.setCellStyle(headerStyle);

            for (int i = 0; i < NOTES.length; i++) {
                Row row = help.createRow(i + 1);
                row.createCell(0).setCellValue(NOTES[i][0]);
                row.createCell(1).setCellValue(NOTES[i][1]);
            }

            Row footer = help.createRow(NOTES.length + 2);
            footer.createCell(0).setCellValue("Note");
            footer.createCell(1).setCellValue(
                    "Delete the example row before uploading. Row 1 must stay as the header. "
                            + "Invalid rows are reported individually and skipped - valid rows still import.");

            help.autoSizeColumn(0);
            help.autoSizeColumn(1);

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalOperationException("Could not generate the Excel template: " + e.getMessage());
        }
    }
}
