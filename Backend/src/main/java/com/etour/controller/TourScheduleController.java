package com.etour.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.etour.entity.Tour;
import com.etour.entity.TourSchedule;
import com.etour.service.TourScheduleService;
import org.springframework.security.access.prepost.PreAuthorize;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tour-schedules")
public class TourScheduleController {

    @Autowired
    private TourScheduleService scheduleService;

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Tour Schedule Controller Working");
    }

    @PostMapping("/tour/{tourId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TourSchedule> createSchedule(
            @PathVariable("tourId") Long tourId,
            @Valid @RequestBody TourSchedule schedule) {

        Tour tour = new Tour();
        tour.setTourId(tourId);
        schedule.setTour(tour);

        TourSchedule savedSchedule = scheduleService.createSchedule(schedule);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedSchedule);
    }

    @GetMapping
    public ResponseEntity<List<TourSchedule>> getAllSchedules() {
        return ResponseEntity.ok(scheduleService.getAllSchedules());
    }

    @GetMapping("/{scheduleId}")
    public ResponseEntity<TourSchedule> getScheduleById(
            @PathVariable("scheduleId") Long scheduleId) {

        return ResponseEntity.ok(scheduleService.getSchedule(scheduleId));
    }

    @GetMapping("/tour/{tourId}")
    public ResponseEntity<List<TourSchedule>> getSchedulesByTourId(
            @PathVariable("tourId") Long tourId) {

        return ResponseEntity.ok(scheduleService.getSchedulesByTourId(tourId));
    }

    @PutMapping("/{scheduleId}/tour/{tourId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TourSchedule> updateSchedule(
            @PathVariable("scheduleId") Long scheduleId,
            @PathVariable("tourId") Long tourId,
            @Valid @RequestBody TourSchedule schedule) {

        Tour tour = new Tour();
        tour.setTourId(tourId);
        schedule.setTour(tour);

        return ResponseEntity.ok(
                scheduleService.updateSchedule(scheduleId, schedule));
    }

    @DeleteMapping("/{scheduleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteSchedule(
            @PathVariable("scheduleId") Long scheduleId) {

        scheduleService.deleteSchedule(scheduleId);
        return ResponseEntity.ok("Tour Schedule deleted successfully.");
    }
}