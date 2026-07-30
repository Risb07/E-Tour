package com.etour.service;

import java.util.List;

import com.etour.entity.Review;

public interface ReviewService {
	
	
	// Review CRUD
	public Review addReview(Review review);
	
	public List<Review> getAllReview();
	
	public Review getById(long id);
	
	public Review editReview(long id, Review reviewDetails);
	
	public void deleteReview(long id);
	
	
	
	// Customer CRUD
	public List<Review> getByCustomerId(long id);
	
	
	
	// Customer and Tour CRUD
	public Review addByCustomerIdTourId(long customer_id, long tour_id, Review review);
	
	public Review editByCustomerIdTourId(long customer_id, long tour_id, Review reviewDetails);
	
	
	
	// Tour CRUD
	public List<Review> getByTourID(long id);
	
	public List<Review> getByRating(int id);
	
	// Rating
	public List<Review> sortByRating();
	
	public List<Review> top5Reviews(long tourId);
	
	Double getAverageRating(long tourId);
	
}
