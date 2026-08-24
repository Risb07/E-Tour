package com.etour.controller;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.etour.dto.TourSearchResultDto;
import com.etour.service.TourSearchService;

/**
 * BRD 3.6 Search: name, code, category, price range, duration and period
 * (from/to dates), with pagination and sorting. Public - browsing tours
 * doesn't require a login.
 *
 * Query/enrichment logic lives in TourSearchService; this class only maps
 * request params onto it.
 */
@RestController
@RequestMapping("/api/tours/search")
public class TourSearchController {

    // Whitelisted so a crafted sortBy can't reference an arbitrary field and
    // produce a 500 (or leak schema details through the error).
    private static final java.util.Set<String> SORTABLE =
            java.util.Set.of("tourId", "title", "basePrice", "durationDays");

    private final TourSearchService tourSearchService;

    public TourSearchController(TourSearchService tourSearchService) {
        this.tourSearchService = tourSearchService;
    }

    @GetMapping
    public ResponseEntity<Page<TourSearchResultDto>> search(
            @RequestParam(required = false) String tourName,
            @RequestParam(required = false) String tourCode,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer minDuration,
            @RequestParam(required = false) Integer maxDuration,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "tourId") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        String safeSortBy = SORTABLE.contains(sortBy) ? sortBy : "tourId";
        // Cap page size so a caller can't request the entire table in one go.
        int safeSize = Math.min(Math.max(size, 1), 50);

        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, safeSortBy);
        Pageable pageable = PageRequest.of(Math.max(page, 0), safeSize, sort);

        return ResponseEntity.ok(tourSearchService.search(
                tourName, tourCode, categoryId, minPrice, maxPrice,
                minDuration, maxDuration, startDate, endDate, pageable));
    }
}
