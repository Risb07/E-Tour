package com.etour.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.etour.entity.Tour;

@Repository
public interface TourRepository extends JpaRepository<Tour, Long> {

}
