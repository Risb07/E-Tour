package com.etour.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.etour.dto.ReviewRequest;
import com.etour.entity.Review;
import com.etour.service.ReviewService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

private final ReviewService reviewService;
	
	public ReviewController(ReviewService reviewService) {
		this.reviewService = reviewService;
	}
	
	@GetMapping
	public List<Review> getAllReview(){
		return reviewService.getAllReview();
	}
	
	@GetMapping("{id}")
	public Review getById(@Valid @PathVariable("id") long id) {
		return reviewService.getById(id);
	}
	
	// ----- Moderation endpoints (ADMIN only) -----
	// These act on any review by id. They are deliberately NOT available to
	// customers - a customer manages their own review through /me/** below.

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public Review addReview(@Valid @RequestBody Review review) {
		return reviewService.addReview(review);
	}

	@PutMapping("{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public Review editReview(@Valid @PathVariable("id") long id, @RequestBody Review review) {
		return reviewService.editReview(id, review);
	}

	@DeleteMapping("{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public void deleteReview(@Valid @PathVariable("id") long id) {
		reviewService.deleteReview(id);
	}

	@GetMapping("/customer/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public List<Review> getByCustomerId(@Valid @PathVariable("id") long id){
		return reviewService.getByCustomerId(id);
	}

	// REMOVED (IDOR): POST/PUT /customer/{customer_id}/tour/{tour_id}.
	// They took the customer id from the URL, so any authenticated user could
	// create or edit a review as any other customer. Callers must use
	// POST/PUT /api/reviews/me/tour/{tourId}, which derives the customer from
	// the JWT principal and additionally requires a real booking for the tour.

	@GetMapping("/tour/{id}")
	public List<Review> getByTourId(@Valid @PathVariable("id") long id){
		return reviewService.getByTourID(id);
	}
	
	@GetMapping("/rating/{id}")
	public List<Review> getByRating(@Valid @PathVariable("id") Integer id){
		return reviewService.getByRating(id);
	}
	
	@GetMapping("/rating/sort")
	public List<Review> sortByRating(){
		return reviewService.sortByRating();
	}
	
	@GetMapping("/tour/{id}/top5")
	public List<Review> top5Reviews(@PathVariable("id") long id){
		return reviewService.top5Reviews(id);
	}
	
	
	@GetMapping("/tour/{id}/avg")
	public Double getAverageRating(@PathVariable("id") long id) {
		return reviewService.getAverageRating(id);
	}
	
	// Own-review endpoints - customer identity comes from the JWT principal,
	// never from the request body or a path id.
	@PostMapping("/me/tour/{tourId}")
	@PreAuthorize("hasRole('CUSTOMER')")
	public Review addMyReview(@PathVariable("tourId") long tourId, @Valid @RequestBody ReviewRequest request) {
		return reviewService.addMyReview(tourId, request);
	}

	@PutMapping("/me/tour/{tourId}")
	@PreAuthorize("hasRole('CUSTOMER')")
	public Review editMyReview(@PathVariable("tourId") long tourId, @Valid @RequestBody ReviewRequest request) {
		return reviewService.editMyReview(tourId, request);
	}
	
	@DeleteMapping("/me/{reviewId}")
	@PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
	public void deleteMyReview(@PathVariable("reviewId") long reviewId) {
		reviewService.deleteMyReview(reviewId);
	}
	
}
