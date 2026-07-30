package com.etour.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.etour.entity.Tour;
import com.etour.enums.TourCode;

@Repository
public interface TourRepository extends JpaRepository<Tour, Long> {

      List<Tour> findByTourCode(TourCode tourCode);
}
