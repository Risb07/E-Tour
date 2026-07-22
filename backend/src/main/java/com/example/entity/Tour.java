package com.example.entity;

import com.example.enums.TourCode;
import com.example.enums.TourStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tour")
public class Tour {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tour_id")
	private int tour_id;

    @Column(nullable = false,length = 200)
	private String title;

    @Column(columnDefinition = "TEXT")
	private String description;

    @Column(name="duration_days",nullable = false)
	private int duration_cays;

    @Column(name="base_price",nullable = false,precision = 10,scale = 2)
	private int base_price;

    @Column(name="tour_code",unique = true)
	private TourCode tour_code;

    @Enumerated(EnumType.STRING)
	private TourStatus status;

	public int getTour_id() {
		return tour_id;
	}

	public void setTour_id(int tour_id) {
		this.tour_id = tour_id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getDuration_cays() {
		return duration_cays;
	}

	public void setDuration_cays(int duration_cays) {
		this.duration_cays = duration_cays;
	}

	public int getBase_price() {
		return base_price;
	}

	public void setBase_price(int base_price) {
		this.base_price = base_price;
	}

	public TourCode getTour_code() {
		return tour_code;
	}

	public void setTour_code(TourCode tour_code) {
		this.tour_code = tour_code;
	}

	public TourStatus getStatus() {
		return status;
	}

	public void setStatus(TourStatus status) {
		this.status = status;
	}

}
