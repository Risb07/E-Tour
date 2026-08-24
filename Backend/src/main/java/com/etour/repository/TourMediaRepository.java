package com.etour.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.etour.entity.TourMedia;

public interface TourMediaRepository extends JpaRepository<TourMedia, Long> {
    List<TourMedia> findByTour_TourIdAndStatusTrue(Long tourId);

    /**
     * Batch-loads media for a page of search results in one query, so picking
     * a thumbnail per result doesn't cost one query per tour.
     */
    List<TourMedia> findByTour_TourIdInAndStatusTrueOrderByDisplayOrderAsc(List<Long> tourIds);
}
