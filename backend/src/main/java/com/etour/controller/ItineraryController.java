package com.etour.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.etour.entity.Itinerary;
import com.etour.service.ItineraryService;

@RestController
@RequestMapping("/api/Itinerary")
public class ItineraryController {

	private final ItineraryService ItineraryService;

	public ItineraryController(ItineraryService ItineraryService) {
		this.ItineraryService = ItineraryService;
	}

	@GetMapping
	public List<Itinerary> getItinerary() {
		return ItineraryService.getItinerary();
	}

	@PostMapping
	public Itinerary createItinerary(@RequestBody Itinerary Itinerary) {
		return ItineraryService.saveItinerary(Itinerary);
	}

	@GetMapping("/{id}")
	public Itinerary getById(@PathVariable("id") Long id) {
		return ItineraryService.getById(id);
	}

	@PutMapping("/{id}")
	public Itinerary updateItinerary(@PathVariable Long id,
			@RequestBody Itinerary Itinerary) {

		return ItineraryService.updateItinerary(id, Itinerary);

	}

	@DeleteMapping("/{id}")
	public String deleteItinerary(@PathVariable Long id) {
		ItineraryService.deleteItinerary(id);
		return "Itinerary deleted successfully";
	}

}
