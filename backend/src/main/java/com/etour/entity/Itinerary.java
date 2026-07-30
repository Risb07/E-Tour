package com.etour.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
public class Itinerary {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long itineraryId;

	@NotNull
	private Long tourId;

	private int dayNumber;
	private String title;
	private String description;

	public Itinerary() {
	}

	public Itinerary(Long itineraryId, Long tourId, int dayNumber,
			String title, String description) {
		this.itineraryId = itineraryId;
		this.tourId = tourId;
		this.dayNumber = dayNumber;
		this.title = title;
		this.description = description;
	}

	public Long getItineraryId() {
		return itineraryId;
	}

	public void setItineraryId(Long itineraryId) {
		this.itineraryId = itineraryId;
	}

	public Long getTourId() {
		return tourId;
	}

	public void setTourId(Long tourId) {
		this.tourId = tourId;
	}

	public int getDayNumber() {
		return dayNumber;
	}

	public void setDayNumber(int dayNumber) {
		this.dayNumber = dayNumber;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}