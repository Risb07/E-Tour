package com.etour.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.etour.entity.Itinerary;
import com.etour.exception.ResourceNotFoundException;
import com.etour.repository.ItineraryRepository;
import com.etour.service.ItineraryService;

@Service
public class ItineraryServiceImpl implements ItineraryService {

	private final ItineraryRepository ItineraryRepository;

	public ItineraryServiceImpl(ItineraryRepository ItineraryRepository) {
		this.ItineraryRepository = ItineraryRepository;
	}

	public List<Itinerary> getItinerary() {
		return ItineraryRepository.findAll();

	}

	public Itinerary getById(Long id) {
		return ItineraryRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Not Found"));
	}

	public Itinerary saveItinerary(Itinerary Itinerary) {
		return ItineraryRepository.save(Itinerary);
	}

	public Itinerary updateItinerary(Long id, Itinerary updatedItinerary) {

		Itinerary existing = ItineraryRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Itinerary not found"));

		existing.setTourId(updatedItinerary.getTourId());
		existing.setDayNumber(updatedItinerary.getDayNumber());
		existing.setTitle(updatedItinerary.getTitle());
		existing.setDescription(updatedItinerary.getDescription());

		return ItineraryRepository.save(existing);
	}

	public void deleteItinerary(Long id) {
		ItineraryRepository.deleteById(id);
	}

}
