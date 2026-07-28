package com.etour.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.etour.entity.TourSchedule;


public interface TourScheduleRepository extends JpaRepository<TourSchedule, Long> {

	List<TourSchedule> findByTourTourId(Long tourId);

}
