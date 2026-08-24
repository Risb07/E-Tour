package com.etour.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "itinerary")
public class Itinerary {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "itinerary_id")
	private Long itineraryId;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "tour_id", nullable = false)
	@JsonIgnoreProperties({ "categories", "tourCosts", "hibernateLazyInitializer", "handler" })
	private Tour tour;

	@Column(name = "day_number")
	private int dayNumber;
	private String title;
	private String description;

	public Itinerary() {
	}

	public Itinerary(Long itineraryId, Tour tour, int dayNumber,
			String title, String description) {
		this.itineraryId = itineraryId;
		this.tour = tour;
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

	public Tour getTour() {
		return tour;
	}

	public void setTour(Tour tour) {
		this.tour = tour;
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
