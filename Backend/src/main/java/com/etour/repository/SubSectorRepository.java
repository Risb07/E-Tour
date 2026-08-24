package com.etour.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.etour.entity.SubSector;

public interface SubSectorRepository extends JpaRepository<SubSector, Long> {

    List<SubSector> findBySector_SectorIdAndActiveTrueOrderBySortOrderAsc(Long sectorId);

    List<SubSector> findBySector_SectorIdOrderBySortOrderAsc(Long sectorId);

    List<SubSector> findByActiveTrueOrderBySortOrderAsc();
}
