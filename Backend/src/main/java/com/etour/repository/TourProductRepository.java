package com.etour.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.etour.entity.TourProduct;

public interface TourProductRepository extends JpaRepository<TourProduct, Long> {

    List<TourProduct> findBySubSector_SubSectorIdAndActiveTrueOrderBySortOrderAsc(Long subSectorId);

    List<TourProduct> findBySubSector_SubSectorIdOrderBySortOrderAsc(Long subSectorId);

    List<TourProduct> findBySubSector_Sector_SectorIdAndActiveTrueOrderBySortOrderAsc(Long sectorId);

    List<TourProduct> findByActiveTrueOrderBySortOrderAsc();

    /**
     * Used by the multipath generator to stay idempotent - a product linking
     * this tour into this sub-sector is only created if one doesn't exist.
     */
    boolean existsBySubSector_SubSectorIdAndTour_TourId(Long subSectorId, Long tourId);
}
