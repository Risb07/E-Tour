package com.etour.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.etour.entity.Customer;
import com.etour.entity.Review;
import com.etour.entity.Tour;
import com.etour.exception.ResourceNotFoundException;
import com.etour.repository.CustomerRepository;
import com.etour.repository.ReviewRepository;
import com.etour.repository.TourRepository;
import com.etour.service.ReviewService;


@Service
public class ReviewServiceImpl implements ReviewService {

private final ReviewRepository reviewRepository;
private final TourRepository tourRepository;
private final CustomerRepository customerRepository;
	
	public ReviewServiceImpl(ReviewRepository reviewRepository, TourRepository tourRepository, CustomerRepository customerRepository) {
		this.reviewRepository = reviewRepository;
		this.tourRepository = tourRepository;
		this.customerRepository = customerRepository;
	}
	
	
	public Review addReview(Review review) {
		return reviewRepository.save(review);
	}
	
	public List<Review> getAllReview(){
		return reviewRepository.findAll();
	}
	
	public Review getById(long id) {
		return reviewRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Review not Found"));
	}
	
	public Review editReview(long id, Review reviewDetails) {
		Review review = reviewRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Review Not Found"));
		review.setRating(reviewDetails.getRating());
		review.setComment(reviewDetails.getComment());
		return reviewRepository.save(review);
	}
	
	public void deleteReview(long id) {
		Review review = reviewRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Review Not Found"));
		reviewRepository.delete(review);
	}
	
	// Customers
	
	public List<Review> getByCustomerId(long id) {
		customerRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Customer Not Found"));
		List<Review> review = reviewRepository.findByCustomerCustomerId(id);
		if(review.isEmpty()) {
			throw new ResourceNotFoundException("No Review Found for this specific Customer");
		}
		return review;
	}
	
	
	// Customers and Tour 
	
	public Review addByCustomerIdTourId(long customer_id, long tour_id, Review review) {
		Customer customer = customerRepository.findById(customer_id).orElseThrow(()-> new ResourceNotFoundException("Customer Not Found"));
		Tour tour = tourRepository.findById((long) tour_id).orElseThrow(()-> new ResourceNotFoundException("Tour Not Found"));
		if(reviewRepository.findByCustomerCustomerIdAndTourTourId(customer_id, tour_id).isPresent()){
			throw new RuntimeException("Customer has already reviewed this tour");
		}
		review.setCustomer(customer);
		review.setTour(tour);
		return reviewRepository.save(review);
	}
	
	
	public Review editByCustomerIdTourId(long customer_id, long tour_id, Review reviewDetails) {
		customerRepository.findById(customer_id).orElseThrow(()-> new ResourceNotFoundException("Customer Not Found"));
		tourRepository.findById((long) tour_id).orElseThrow(()-> new ResourceNotFoundException("Tour Not Found"));
		Review review = reviewRepository.findByCustomerCustomerIdAndTourTourId(customer_id, tour_id).orElseThrow(() -> new ResourceNotFoundException("Review not found"));
		review.setRating(reviewDetails.getRating());
		review.setComment(reviewDetails.getComment());
		return reviewRepository.save(review);
	}
	
	
	// Tours 
	
	public List<Review> getByTourID(long id){
		tourRepository.findById((long)id).orElseThrow(()-> new ResourceNotFoundException("Tour Not Found"));
		return reviewRepository.findByTourTourId(id);
	}
	
	public List<Review> getByRating(int id){
		return reviewRepository.findByRating(id);
	}
	
	
	// Rating
	
	public List<Review> sortByRating(){
		return reviewRepository.findAllByOrderByRatingDesc();
	}
	
	
	public List<Review> top5Reviews(long tourId) {
		return reviewRepository.findTop5ReviewsByTourTourId(tourId);
	}
	
	public Double getAverageRating(long tourId) {
		return reviewRepository.getAverageRating(tourId);
	}

	
	
}
