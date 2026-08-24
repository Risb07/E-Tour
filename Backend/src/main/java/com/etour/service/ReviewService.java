package com.etour.service;

import java.util.List;

import com.etour.dto.ReviewRequest;
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
	
	
	
	// NOTE: addByCustomerIdTourId / editByCustomerIdTourId were removed - they
	// took the customer id from the URL, so any logged-in user could post or
	// edit a review as somebody else (IDOR). Use the /me operations below,
	// which resolve the customer from the JWT principal instead.

	// Own-review operations resolved from the authenticated principal.
	public Review addMyReview(long tourId, ReviewRequest request);

	public Review editMyReview(long tourId, ReviewRequest request);

	public void deleteMyReview(long reviewId);
	
	
	
	// Tour CRUD
	public List<Review> getByTourID(long id);
	
	public List<Review> getByRating(int id);
	
	// Rating
	public List<Review> sortByRating();
	
	public List<Review> top5Reviews(long tourId);
	
	Double getAverageRating(long tourId);
	
}
