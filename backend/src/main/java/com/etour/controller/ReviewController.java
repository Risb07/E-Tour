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
	
	@PostMapping
	public Review addReview(@Valid @RequestBody Review review) {
		return reviewService.addReview(review);
	}
	
	@PutMapping("{id}")
	public Review editReview(@Valid @PathVariable("id") long id, @RequestBody Review review) {
		return reviewService.editReview(id, review);
	}
	
	@DeleteMapping("{id}")
	public void deleteReview(@Valid @PathVariable("id") long id) {
		reviewService.deleteReview(id);
	}
	
	@GetMapping("/customer/{id}")
	public List<Review> getByCustomerId(@Valid @PathVariable("id") long id){
		return reviewService.getByCustomerId(id);
	}
	
	@PostMapping("/customer/{customer_id}/tour/{tour_id}")
	public Review addByCustomerIdTourId(@Valid 
			@PathVariable("customer_id") long customer_id,
			@PathVariable("tour_id") long tour_id,
			@RequestBody Review review) {
		return reviewService.addByCustomerIdTourId(customer_id, tour_id, review);
	}
	
	@PutMapping("/customer/{customer_id}/tour/{tour_id}")
	public Review editByCustomerIdTourId(@Valid 
			@PathVariable("customer_id") long customer_id, 
			@PathVariable("tour_id") long tour_id, 
			@RequestBody Review review) {
		return reviewService.editByCustomerIdTourId(customer_id, tour_id, review);
	}
	
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
	
	
}
