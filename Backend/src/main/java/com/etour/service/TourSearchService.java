package com.etour.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.etour.dto.TourSearchResultDto;

public interface TourSearchService {

    /**
     * BRD 3.6 search. Every filter is optional; date filters implement
     * "Search on Period" (tours departing on/after startDate and returning
     * on/before endDate).
     */
    Page<TourSearchResultDto> search(String tourName, String tourCode, Long categoryId,
            BigDecimal minPrice, BigDecimal maxPrice,
            Integer minDuration, Integer maxDuration,
            LocalDate startDate, LocalDate endDate,
            Pageable pageable);
}
