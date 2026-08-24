package com.etour.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.etour.dto.TourSearchResultDto;
import com.etour.entity.Tour;
import com.etour.entity.TourMedia;
import com.etour.entity.TourSchedule;
import com.etour.enums.MediaType;
import com.etour.enums.TourCode;
import com.etour.enums.TourStatus;
import com.etour.repository.ReviewRepository;
import com.etour.repository.TourMediaRepository;
import com.etour.repository.TourRepository;
import com.etour.repository.TourScheduleRepository;
import com.etour.service.TourSearchService;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

/**
 * BRD 3.6 search. Only ACTIVE tours are searchable - draft/inactive tours
 * shouldn't surface to a public visitor regardless of filters.
 *
 * Result enrichment (departure dates, thumbnail, rating) is done with three
 * batch queries keyed on the page's tour ids, so a page of N results costs a
 * constant number of queries rather than 1 + 3N.
 */
@Service
public class TourSearchServiceImpl implements TourSearchService {

    private final TourRepository tourRepository;
    private final TourScheduleRepository tourScheduleRepository;
    private final TourMediaRepository tourMediaRepository;
    private final ReviewRepository reviewRepository;

    public TourSearchServiceImpl(TourRepository tourRepository,
            TourScheduleRepository tourScheduleRepository,
            TourMediaRepository tourMediaRepository,
            ReviewRepository reviewRepository) {
        this.tourRepository = tourRepository;
        this.tourScheduleRepository = tourScheduleRepository;
        this.tourMediaRepository = tourMediaRepository;
        this.reviewRepository = reviewRepository;
    }

    @Override
    public Page<TourSearchResultDto> search(String tourName, String tourCode, Long categoryId,
            BigDecimal minPrice, BigDecimal maxPrice,
            Integer minDuration, Integer maxDuration,
            LocalDate startDate, LocalDate endDate,
            Pageable pageable) {

        Specification<Tour> spec = (root, query, cb) -> cb.equal(root.get("status"), TourStatus.ACTIVE);

        if (tourName != null && !tourName.isBlank()) {
            String like = "%" + tourName.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("title")), like));
        }
        if (tourCode != null && !tourCode.isBlank()) {
            // An unknown code would blow up valueOf with a 500; treat it as
            // "no tour matches" instead, which is what a user would expect.
            TourCode code = parseTourCode(tourCode);
            if (code == null) {
                return Page.empty(pageable);
            }
            final TourCode resolved = code;
            spec = spec.and((root, query, cb) -> cb.equal(root.get("tourCode"), resolved));
        }
        if (categoryId != null) {
            spec = spec.and((root, query, cb) -> {
                query.distinct(true);
                return cb.equal(root.join("categories").get("categoryId"), categoryId);
            });
        }
        if (minPrice != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("basePrice"), minPrice));
        }
        if (maxPrice != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("basePrice"), maxPrice));
        }
        if (minDuration != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("durationDays"), minDuration));
        }
        if (maxDuration != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("durationDays"), maxDuration));
        }

        // BRD 3.6 "Search on Period" - a tour matches if it has at least one
        // schedule that both departs on/after startDate and returns on/before
        // endDate (i.e. the whole trip fits inside the requested window).
        if (startDate != null || endDate != null) {
            spec = spec.and((root, query, cb) -> {
                query.distinct(true);
                Subquery<Long> sub = query.subquery(Long.class);
                Root<TourSchedule> schedule = sub.from(TourSchedule.class);
                sub.select(schedule.get("tour").get("tourId"));
                Predicate pred;
                if (startDate != null && endDate != null) {
                    pred = cb.and(
                            cb.greaterThanOrEqualTo(schedule.get("departureDate"), startDate),
                            cb.lessThanOrEqualTo(schedule.get("returnDate"), endDate));
                } else if (startDate != null) {
                    pred = cb.greaterThanOrEqualTo(schedule.get("departureDate"), startDate);
                } else {
                    pred = cb.lessThanOrEqualTo(schedule.get("returnDate"), endDate);
                }
                sub.where(pred);
                return cb.in(root.get("tourId")).value(sub);
            });
        }

        Page<Tour> tours = tourRepository.findAll(spec, pageable);
        if (tours.isEmpty()) {
            return tours.map(this::toDto);
        }

        List<Long> tourIds = tours.getContent().stream().map(Tour::getTourId).toList();

        Map<Long, TourSchedule> nearestSchedule = loadNearestSchedules(tourIds, startDate, endDate);
        Map<Long, String> thumbnails = loadThumbnails(tourIds);
        Map<Long, double[]> ratings = loadRatingSummaries(tourIds);

        return tours.map(tour -> {
            TourSearchResultDto dto = toDto(tour);
            dto.setImageUrl(thumbnails.get(tour.getTourId()));

            TourSchedule schedule = nearestSchedule.get(tour.getTourId());
            if (schedule != null) {
                dto.setScheduleId(schedule.getScheduleId());
                dto.setDepartureDate(schedule.getDepartureDate());
                dto.setReturnDate(schedule.getReturnDate());
                dto.setAvailableSeats(schedule.getAvailableSeats());
                dto.setSchedulePrice(schedule.getPrice());
            }

            double[] rating = ratings.get(tour.getTourId());
            if (rating != null) {
                dto.setAverageRating(rating[0]);
                dto.setTotalReviews((long) rating[1]);
            } else {
                dto.setTotalReviews(0L);
            }
            return dto;
        });
    }

    /** One query for the whole page; keeps the earliest schedule per tour. */
    private Map<Long, TourSchedule> loadNearestSchedules(List<Long> tourIds, LocalDate startDate, LocalDate endDate) {
        // With no explicit start date, "nearest" means the next departure from
        // today - showing a departure that has already left would be wrong.
        LocalDate from = startDate != null ? startDate : LocalDate.now();

        Map<Long, TourSchedule> byTour = new HashMap<>();
        for (TourSchedule schedule : tourScheduleRepository.findForToursInWindow(tourIds, from, endDate)) {
            // Query is ordered by departureDate asc, so the first one seen for
            // a tour is the nearest.
            byTour.putIfAbsent(schedule.getTour().getTourId(), schedule);
        }
        return byTour;
    }

    /** One query; first IMAGE by display order wins as the card thumbnail. */
    private Map<Long, String> loadThumbnails(List<Long> tourIds) {
        Map<Long, String> byTour = new HashMap<>();
        for (TourMedia media : tourMediaRepository.findByTour_TourIdInAndStatusTrueOrderByDisplayOrderAsc(tourIds)) {
            if (media.getMediaType() == MediaType.IMAGE) {
                byTour.putIfAbsent(media.getTour().getTourId(), media.getFilePath());
            }
        }
        return byTour;
    }

    /** One grouped query -> { tourId: [avgRating, reviewCount] }. */
    private Map<Long, double[]> loadRatingSummaries(List<Long> tourIds) {
        Map<Long, double[]> byTour = new HashMap<>();
        for (Object[] row : reviewRepository.findRatingSummariesForTours(tourIds)) {
            Long tourId = ((Number) row[0]).longValue();
            double avg = row[1] == null ? 0d : ((Number) row[1]).doubleValue();
            double count = row[2] == null ? 0d : ((Number) row[2]).doubleValue();
            byTour.put(tourId, new double[] { avg, count });
        }
        return byTour;
    }

    private TourCode parseTourCode(String raw) {
        try {
            return TourCode.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private TourSearchResultDto toDto(Tour tour) {
        TourSearchResultDto dto = new TourSearchResultDto();
        dto.setTourId(tour.getTourId());
        dto.setTitle(tour.getTitle());
        dto.setDescription(tour.getDescription());
        dto.setTourCode(tour.getTourCode() == null ? null : tour.getTourCode().name());
        dto.setDurationDays(tour.getDurationDays());
        dto.setBasePrice(tour.getBasePrice());
        return dto;
    }
}
