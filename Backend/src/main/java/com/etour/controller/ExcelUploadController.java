package com.etour.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.etour.dto.ExcelUploadResult;
import com.etour.service.impl.ExcelUploadServiceImpl;
import com.etour.util.TourExcelTemplateGenerator;

@RestController
@RequestMapping("/api/excel-upload")
public class ExcelUploadController {

    private final ExcelUploadServiceImpl excelUploadService;
    private final TourExcelTemplateGenerator templateGenerator;

    public ExcelUploadController(ExcelUploadServiceImpl excelUploadService,
            TourExcelTemplateGenerator templateGenerator) {
        this.excelUploadService = excelUploadService;
        this.templateGenerator = templateGenerator;
    }

    /**
     * Downloadable .xlsx template. Generated from the same column constant the
     * parser uses, so the template can't drift from what the importer expects.
     * Admin-only via SecurityConfig (/api/excel-upload/**).
     */
    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        byte[] workbook = templateGenerator.generate();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"etour-tour-upload-template.xlsx\"")
                .body(workbook);
    }

    // Admin-only, enforced in SecurityConfig (/api/excel-upload/**).
    // Expected columns: title | description | durationDays | basePrice |
    // tourCode | categoryName | status
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ExcelUploadResult> upload(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(excelUploadService.upload(file));
    }
}
