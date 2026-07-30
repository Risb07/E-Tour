package com.etour.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Review {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "review_id")
	private Long reviewId;
	
	@JsonIgnoreProperties({
		"title", "description", "durationDays", "basePrice", "tourCode", "status", "categories"
	})
	@ManyToOne(optional = false)
	@JoinColumn(name = "tour_id", nullable = false)
	@NotNull(message = "Tour cannot be empty")
	private Tour tour;
	
	@JsonIgnoreProperties({
		"email", "phone"
	})
	@ManyToOne(optional = false)
	@JoinColumn(name = "customer_id", nullable = false)
	@NotNull(message = "customer_id cannot be empty")
	private Customer customer;
	
	@Max(value = 5,message = "rating cannot be above 5")
	@Min(value = 1, message = "rating cannot be below 1")
	private Integer rating;
	
	@NotBlank(message = "comment cannot be blank")
	private String comment;

	public Long getReviewId() {
		return reviewId;
	}

	public void setReviewId(Long reviewId) {
		this.reviewId = reviewId;
	}

	public Tour getTour() {
		return tour;
	}

	public void setTour(Tour tour) {
		this.tour = tour;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public int getRating() {
		return rating;
	}

	public void setRating(Integer rating) {
		this.rating = rating;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
	
}
