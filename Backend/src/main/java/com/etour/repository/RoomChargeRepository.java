package com.etour.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.etour.entity.RoomCharge;
import com.etour.enums.Occupancy;

public interface RoomChargeRepository extends JpaRepository<RoomCharge, Long> {

    /** Public: only the options a customer can actually pick. */
    List<RoomCharge> findByTour_TourIdAndActiveTrue(Long tourId);

    /** Admin: includes retired options so they can be re-enabled. */
    List<RoomCharge> findByTour_TourIdOrderByOccupancyAsc(Long tourId);

    Optional<RoomCharge> findByTour_TourIdAndOccupancyAndActiveTrue(Long tourId, Occupancy occupancy);

    boolean existsByTour_TourIdAndOccupancy(Long tourId, Occupancy occupancy);
}
