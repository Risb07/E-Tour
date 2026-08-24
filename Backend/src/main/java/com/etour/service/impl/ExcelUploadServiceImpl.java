package com.etour.service.impl;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.etour.dto.ExcelUploadResult;
import com.etour.entity.Category;
import com.etour.entity.ExcelUploadBatch;
import com.etour.entity.Tour;
import com.etour.enums.TourCode;
import com.etour.enums.TourStatus;
import com.etour.enums.UploadBatchStatus;
import com.etour.exception.IllegalOperationException;
import com.etour.repository.CategoryRepository;
import com.etour.repository.ExcelUploadBatchRepository;
import com.etour.repository.TourRepository;
import com.etour.security.CurrentUserProvider;
import com.etour.util.FileStorageService;

/**
 * BRD Module 16 - Excel Upload. Expected columns (row 1 = header, data from
 * row 2): title | description | durationDays | basePrice | tourCode
 * (ADV/INT/DEV) | categoryName | status (ACTIVE/INACTIVE/DRAFT)
 *
 * Duplicate detection is by exact tour title (case-insensitive) against
 * existing tours AND within the same file. Every row failure is collected
 * into the response rather than aborting the whole batch, so one bad row
 * doesn't block ninety-nine good ones.
 */
@Service
public class ExcelUploadServiceImpl {

    private final TourRepository tourRepository;
    private final CategoryRepository categoryRepository;
    private final ExcelUploadBatchRepository batchRepository;
    private final CurrentUserProvider currentUserProvider;
    private final FileStorageService fileStorageService;
    private final DataFormatter dataFormatter = new DataFormatter();

    public ExcelUploadServiceImpl(TourRepository tourRepository, CategoryRepository categoryRepository,
            ExcelUploadBatchRepository batchRepository, CurrentUserProvider currentUserProvider,
            FileStorageService fileStorageService) {
        this.tourRepository = tourRepository;
        this.categoryRepository = categoryRepository;
        this.batchRepository = batchRepository;
        this.currentUserProvider = currentUserProvider;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public ExcelUploadResult upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalOperationException("Upload file is empty");
        }

        String storedPath = fileStorageService.store(file, "excel-batches");

        ExcelUploadBatch batch = new ExcelUploadBatch();
        batch.setUploadedBy(currentUserProvider.currentUser());
        batch.setFileName(file.getOriginalFilename());
        batch.setFilePath(storedPath);
        batch.setStatus(UploadBatchStatus.PROCESSING);
        batch = batchRepository.save(batch);

        List<String> rowErrors = new ArrayList<>();
        int total = 0;
        int success = 0;
        List<String> seenTitles = new ArrayList<>();

        try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int rowIdx = 1; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
                Row row = sheet.getRow(rowIdx);
                if (row == null || isRowBlank(row)) {
                    continue;
                }
                total++;
                int excelRowNumber = rowIdx + 1; // 1-based, matches what a user sees in Excel

                try {
                    String title = getString(row, 0);
                    String description = getString(row, 1);
                    Integer durationDays = (int) getNumeric(row, 2);
                    BigDecimal basePrice = BigDecimal.valueOf(getNumeric(row, 3));
                    String tourCodeStr = getString(row, 4);
                    String categoryName = getString(row, 5);
                    String statusStr = getString(row, 6);

                    if (title == null || title.isBlank()) {
                        throw new IllegalArgumentException("title is required");
                    }
                    if (tourCodeStr == null || tourCodeStr.isBlank()) {
                        throw new IllegalArgumentException("tourCode is required");
                    }
                    if (basePrice.compareTo(BigDecimal.ZERO) <= 0) {
                        throw new IllegalArgumentException("basePrice must be greater than zero");
                    }
                    if (durationDays == null || durationDays < 1) {
                        throw new IllegalArgumentException("durationDays must be at least 1");
                    }

                    // Duplicate detection: existing DB rows AND earlier rows in this same file.
                    if (tourRepository.existsByTitleIgnoreCase(title)
                            || seenTitles.stream().anyMatch(t -> t.equalsIgnoreCase(title))) {
                        throw new IllegalArgumentException("duplicate tour title '" + title + "'");
                    }

                    Tour tour = new Tour();
                    tour.setTitle(title);
                    tour.setDescription(description);
                    tour.setDurationDays(durationDays);
                    tour.setBasePrice(basePrice);
                    tour.setTourCode(TourCode.valueOf(tourCodeStr.trim().toUpperCase()));
                    tour.setStatus(statusStr == null || statusStr.isBlank()
                            ? TourStatus.DRAFT
                            : TourStatus.valueOf(statusStr.trim().toUpperCase()));

                    if (categoryName != null && !categoryName.isBlank()) {
                        Category category = categoryRepository.findByCategoryName(categoryName.trim())
                                .orElseThrow(() -> new IllegalArgumentException(
                                        "category '" + categoryName + "' does not exist"));
                        tour.getCategories().add(category);
                    }

                    tourRepository.save(tour);
                    seenTitles.add(title);
                    success++;

                } catch (Exception rowEx) {
                    rowErrors.add("Row " + excelRowNumber + ": " + rowEx.getMessage());
                }
            }

        } catch (Exception e) {
            batch.setStatus(UploadBatchStatus.FAILED);
            batch.setTotalRows(total);
            batch.setSuccessRows(success);
            batch.setFailedRows(total - success);
            batchRepository.save(batch);
            throw new IllegalOperationException("Could not read the Excel file: " + e.getMessage());
        }

        int failed = total - success;
        batch.setTotalRows(total);
        batch.setSuccessRows(success);
        batch.setFailedRows(failed);
        batch.setStatus(failed == 0 ? UploadBatchStatus.COMPLETED : UploadBatchStatus.COMPLETED_WITH_ERRORS);
        batchRepository.save(batch);

        return new ExcelUploadResult(batch.getBatchId(), total, success, failed, batch.getStatus().name(), rowErrors);
    }

    private boolean isRowBlank(Row row) {
        for (Cell cell : row) {
            if (cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    private String getString(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        String v = dataFormatter.formatCellValue(cell);
        return v == null || v.isBlank() ? null : v.trim();
    }

    private double getNumeric(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return 0;
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        }
        String v = dataFormatter.formatCellValue(cell);
        try {
            return v == null || v.isBlank() ? 0 : Double.parseDouble(v.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
