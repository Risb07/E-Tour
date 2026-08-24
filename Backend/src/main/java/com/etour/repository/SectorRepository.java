package com.etour.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.etour.entity.Sector;

public interface SectorRepository extends JpaRepository<Sector, Long> {

    List<Sector> findByActiveTrueOrderBySortOrderAsc();

    List<Sector> findAllByOrderBySortOrderAsc();
}
