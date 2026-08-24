package com.etour.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.etour.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

	List<Review> findByCustomerCustomerId(Long customerId);

	List<Review> findByTourTourId(Long tourId);

	List<Review> findByRating(int rating);

	Optional<Review> findByCustomerCustomerIdAndTourTourId(Long customerId, Long tourId);

	List<Review> findAllByOrderByRatingDesc();

	Long countByTourTourId(Long tourId);

	@Query(value = "Select * from review " +
			"where tour_id = :tourId " +
			"order by rating desc " +
			"limit 5", nativeQuery = true)
	List<Review> findTop5ReviewsByTourTourId(@Param("tourId") Long tourId);

	@Query(value = "select avg(rating) " +
			"from review " +
			"where tour_id = :tourId", nativeQuery = true)
	Double getAverageRating(@Param("tourId") Long tourId);

	/**
	 * Average rating + review count for many tours in ONE query, for search
	 * result cards. Returns rows of [tourId, avgRating, reviewCount] - calling
	 * getAverageRating() per result would be an N+1.
	 */
	@Query("select r.tour.tourId, avg(r.rating), count(r) from Review r "
			+ "where r.tour.tourId in :tourIds group by r.tour.tourId")
	List<Object[]> findRatingSummariesForTours(@Param("tourIds") List<Long> tourIds);
}
