package com.etour.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.etour.entity.StayMeal;

public interface StayMealRepository extends JpaRepository<StayMeal, Long> {
    List<StayMeal> findByTour_TourIdOrderByDayNumberAsc(Long tourId);
}
