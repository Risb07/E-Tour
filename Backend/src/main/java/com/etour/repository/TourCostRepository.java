package com.etour.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.etour.entity.TourCost;

public interface TourCostRepository extends JpaRepository<TourCost, Long> {

    List<TourCost> findByTour_TourIdOrderByCostIdDesc(Long tourId);

    // Picks the active cost sheet whose validity window covers the given
    // date (normally the schedule's departure date). If an admin has set up
    // more than one overlapping row, the most recently created one wins.
    Optional<TourCost> findFirstByTour_TourIdAndStatusAndValidFromLessThanEqualAndValidToGreaterThanEqualOrderByCostIdDesc(
            Long tourId, Integer status, LocalDate onOrAfterValidFrom, LocalDate onOrBeforeValidTo);
}
