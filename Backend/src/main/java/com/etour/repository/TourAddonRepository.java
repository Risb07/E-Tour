package com.etour.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.etour.entity.TourAddon;

public interface TourAddonRepository extends JpaRepository<TourAddon, Long> {
    List<TourAddon> findByTour_TourIdAndStatusTrue(Long tourId);

    /**
     * Scoped lookup used whenever a customer SELECTS an add-on.
     *
     * Looking one up by id alone would let a request attach an add-on that
     * belongs to a different tour, or one that has been deactivated - neither
     * is offered by the booking page, but neither was refused either. Putting
     * the tour and the active flag in the query means the constraint is
     * enforced by the lookup itself rather than by a check each caller has to
     * remember to repeat.
     */
    Optional<TourAddon> findByAddonIdAndTour_TourIdAndStatusTrue(Long addonId, Long tourId);
}
