package com.etour.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.etour.entity.JourneyDetail;

public interface JourneyDetailRepository extends JpaRepository<JourneyDetail, Long> {
    List<JourneyDetail> findByTour_TourIdOrderBySequenceNoAsc(Long tourId);
}
