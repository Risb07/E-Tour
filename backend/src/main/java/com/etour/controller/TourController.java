package com.etour.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.etour.entity.Tour;
import com.etour.service.TourService;
import com.etour.dto.response.TourDetailsResponse;;

@RestController
@RequestMapping("/api/tours")
public class TourController {

    @Autowired
    private TourService service;

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Controller Working");
    }

    @PostMapping
    public ResponseEntity<Tour> createTour(@RequestBody Tour tour) {
        Tour createdTour = service.createTour(tour);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTour);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tour> getTour(@PathVariable Long id) {
        Tour tour = service.getTour(id);
        return ResponseEntity.ok(tour);
    }

    @GetMapping
    public ResponseEntity<List<Tour>> getAllTours() {
        List<Tour> tours = service.getAllTours();
        return ResponseEntity.ok(tours);
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<List<Tour>> getToursByCode(@PathVariable String code) {
        List<Tour> tours = service.getTourByTourCode(code);
        return ResponseEntity.ok(tours);
    }
    /* Get all Tour details */

    @GetMapping("/{id}/details")
    public ResponseEntity<TourDetailsResponse> getTourDetails(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                service.getTourDetails(id));
    }
    @PutMapping("/{id}")
    public ResponseEntity<Tour> updateTour(@PathVariable Long id, @RequestBody Tour tour) {
        Tour updatedTour = service.updateTour(id, tour);
        return ResponseEntity.ok(updatedTour);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTour(@PathVariable Long id) {
        service.deleteTour(id);
        return ResponseEntity.ok("Tour deleted successfully");
    }
}