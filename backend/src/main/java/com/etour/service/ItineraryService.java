package com.etour.service;

import java.util.List;

import com.etour.entity.Itinerary;

public interface ItineraryService {

	List<Itinerary> getItinerary();

	Itinerary getById(Long id);

	Itinerary saveItinerary(Itinerary itinerary);

	Itinerary updateItinerary(Long id, Itinerary itinerary);

	void deleteItinerary(Long id);

}