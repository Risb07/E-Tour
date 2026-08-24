package com.etour.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.etour.entity.TourCost;
import com.etour.service.TourCostService;

/**
 * Admin CRUD for tour cost sheets (base/single/extra/child prices plus the
 * validity window). Mirrors the TourSchedule endpoints so the admin UI can
 * manage costs per tour the same way it manages schedules.
 */
@RestController
@RequestMapping("/api/tour-costs")
public class TourCostController {

    private final TourCostService tourCostService;

    public TourCostController(TourCostService tourCostService) {
        this.tourCostService = tourCostService;
    }

    @GetMapping
    public ResponseEntity<List<TourCost>> getAllCosts() {
        return ResponseEntity.ok(tourCostService.getAllCosts());
    }

    @GetMapping("/{costId}")
    public ResponseEntity<TourCost> getCostById(@PathVariable Long costId) {
        return ResponseEntity.ok(tourCostService.getCost(costId));
    }

    @GetMapping("/tour/{tourId}")
    public ResponseEntity<List<TourCost>> getCostsByTourId(@PathVariable Long tourId) {
        return ResponseEntity.ok(tourCostService.getCostsByTourId(tourId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/tour/{tourId}")
    public ResponseEntity<TourCost> createCost(@PathVariable Long tourId,
            @RequestBody TourCost cost) {
        TourCost saved = tourCostService.createCost(tourId, cost);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{costId}/tour/{tourId}")
    public ResponseEntity<TourCost> updateCost(@PathVariable Long costId,
            @PathVariable Long tourId, @RequestBody TourCost cost) {
        return ResponseEntity.ok(tourCostService.updateCost(costId, tourId, cost));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{costId}")
    public ResponseEntity<String> deleteCost(@PathVariable Long costId) {
        tourCostService.deleteCost(costId);
        return ResponseEntity.ok("Tour cost deleted successfully");
    }
}
