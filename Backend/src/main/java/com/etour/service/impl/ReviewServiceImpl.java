package com.etour.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.etour.dto.ReviewRequest;
import com.etour.entity.Customer;
import com.etour.entity.Review;
import com.etour.entity.Tour;
import com.etour.enums.BookingStatus;
import com.etour.exception.IllegalOperationException;
import com.etour.exception.ResourceNotFoundException;
import com.etour.repository.BookingRepository;
import com.etour.repository.CustomerRepository;
import com.etour.repository.ReviewRepository;
import com.etour.repository.TourRepository;
import com.etour.security.CurrentUserProvider;
import com.etour.service.ReviewService;


@Service
public class ReviewServiceImpl implements ReviewService {

private final ReviewRepository reviewRepository;
private final TourRepository tourRepository;
private final CustomerRepository customerRepository;
private final BookingRepository bookingRepository;
private final CurrentUserProvider currentUserProvider;

	public ReviewServiceImpl(ReviewRepository reviewRepository, TourRepository tourRepository, CustomerRepository customerRepository, BookingRepository bookingRepository, CurrentUserProvider currentUserProvider) {
		this.reviewRepository = reviewRepository;
		this.tourRepository = tourRepository;
		this.customerRepository = customerRepository;
		this.bookingRepository = bookingRepository;
		this.currentUserProvider = currentUserProvider;
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
		// A customer who simply hasn't reviewed anything is not an error -
		// this used to 404, which made the admin UI show "not found" for every
		// perfectly valid customer with no reviews yet.
		return reviewRepository.findByCustomerCustomerId(id);
	}
	
	
	// Own-review operations (customer identity always from the JWT principal).
	//
	// The previous addByCustomerIdTourId / editByCustomerIdTourId methods took
	// the customer id straight from the URL and were removed: any logged-in
	// user could review a tour while impersonating another customer.

	/**
	 * A review is only allowed from someone who actually booked the tour.
	 * A cancelled booking doesn't earn the right to review, so only
	 * PENDING/CONFIRMED/COMPLETED bookings count.
	 */
	private void requireBookingFor(Customer customer, long tourId) {
		boolean hasBooked = bookingRepository
				.existsByCustomer_CustomerIdAndSchedule_Tour_TourIdAndBookingStatusIn(
						customer.getCustomerId(), tourId,
						List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED, BookingStatus.COMPLETED));
		if (!hasBooked) {
			throw new IllegalOperationException("You can only review a tour you have booked");
		}
	}

	public Review addMyReview(long tourId, ReviewRequest request) {
		Customer customer = currentUserProvider.currentCustomer();
		Tour tour = tourRepository.findById(tourId).orElseThrow(() -> new ResourceNotFoundException("Tour Not Found"));
		requireBookingFor(customer, tourId);
		if (reviewRepository.findByCustomerCustomerIdAndTourTourId(customer.getCustomerId(), tourId).isPresent()) {
			throw new IllegalOperationException("You have already reviewed this tour");
		}
		// Only rating/comment come from the client - customer and tour are
		// always resolved server-side, so neither can be spoofed.
		Review review = new Review();
		review.setCustomer(customer);
		review.setTour(tour);
		review.setRating(request.getRating());
		review.setComment(request.getComment());
		return reviewRepository.save(review);
	}

	public Review editMyReview(long tourId, ReviewRequest request) {
		Customer customer = currentUserProvider.currentCustomer();
		tourRepository.findById(tourId).orElseThrow(() -> new ResourceNotFoundException("Tour Not Found"));
		Review review = reviewRepository.findByCustomerCustomerIdAndTourTourId(customer.getCustomerId(), tourId)
				.orElseThrow(() -> new ResourceNotFoundException("Review not found"));
		review.setRating(request.getRating());
		review.setComment(request.getComment());
		return reviewRepository.save(review);
	}
	
	public void deleteMyReview(long reviewId) {
		Review review = reviewRepository.findById(reviewId)
				.orElseThrow(() -> new ResourceNotFoundException("Review Not Found"));
		if (!currentUserProvider.isAdmin()) {
			Customer customer = currentUserProvider.currentCustomer();
			if (!review.getCustomer().getCustomerId().equals(customer.getCustomerId())) {
				// 404 on purpose - don't reveal the review exists to a non-owner.
				throw new ResourceNotFoundException("Review Not Found");
			}
		}
		reviewRepository.delete(review);
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
