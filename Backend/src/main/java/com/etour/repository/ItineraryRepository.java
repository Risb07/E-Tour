package com.etour.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.etour.entity.Itinerary;

public interface ItineraryRepository extends JpaRepository<Itinerary, Long> {

      List<Itinerary> findByTour_TourIdOrderByDayNumberAsc(Long tourId);

}
