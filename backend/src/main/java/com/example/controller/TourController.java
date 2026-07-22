package com.example.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.entity.Tour;
import com.example.service.TourService;

@RestController
@RequestMapping("/api/tours")
public class TourController {

	@Autowired
	private  TourService service;
	
	

    @GetMapping("/test")
    public String test() {
        return "Controller Working";
    }


	    @PostMapping
	    public Tour createTour(@RequestBody Tour tour) {
	        return service.createTour(tour);
	    }

	    @GetMapping("/{id}")
	    public Tour getTour(@PathVariable int id) {
	        return service.getTour(id);
	    }

	    @GetMapping
	    public List<Tour> getAllTours() {
	        return service.getAllTours();
	    }

	    @PutMapping("/{id}")
	    public Tour updateTour(@PathVariable int id, @RequestBody Tour tour) {
	        return service.updateTour(id, tour);
	    }

	    @DeleteMapping("/{id}")
	    public String deleteTour(@PathVariable int id) {
	        service.deleteTour(id);
	        return "Tour deleted successfully";
	    }
}
