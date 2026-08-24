package com.etour.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.etour.entity.Category;
import com.etour.entity.Review;
import com.etour.entity.Tour;
import com.etour.entity.TourSchedule;
import com.etour.enums.TourCode;
import com.etour.exception.ResourceNotFoundException; 
import com.etour.repository.CategoryRepository;
import com.etour.repository.ReviewRepository;
import com.etour.repository.TourRepository;
import com.etour.service.TourService;
import com.etour.dto.ReviewSummary;
import com.etour.dto.response.TourDetailsResponse;
import com.etour.entity.Itinerary;
import com.etour.repository.ItineraryRepository;
import com.etour.repository.TourScheduleRepository;

@Service
public class TourServiceImpl implements TourService {

    @Autowired
    private TourRepository tourRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TourScheduleRepository tourScheduleRepository;

    @Autowired
    private ItineraryRepository itineraryRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Override
    public Tour createTour(Tour tour) {

        Set<Category> categories = new HashSet<>();

        if (tour.getCategories() != null) {

            for (Category category : tour.getCategories()) {

                Category existingCategory = categoryRepository
                        .findById(category.getCategoryId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Category not found with id : "
                                        + category.getCategoryId()));

                categories.add(existingCategory);
            }
        }

        tour.setCategories(categories);

        return tourRepository.save(tour);
    }

    @Override
    public Tour updateTour(Long id, Tour tourRequest) {

        Tour existingTour = tourRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tour not found with id : " + id));

        existingTour.setTitle(tourRequest.getTitle());
        existingTour.setDescription(tourRequest.getDescription());
        existingTour.setDurationDays(tourRequest.getDurationDays());
        existingTour.setBasePrice(tourRequest.getBasePrice());
        existingTour.setTourCode(tourRequest.getTourCode());
        existingTour.setStatus(tourRequest.getStatus());

        Set<Category> categories = new HashSet<>();

        if (tourRequest.getCategories() != null) {

            for (Category category : tourRequest.getCategories()) {

                Category existingCategory = categoryRepository
                        .findById(category.getCategoryId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Category not found with id : "
                                        + category.getCategoryId()));

                categories.add(existingCategory);
            }
        }

        existingTour.setCategories(categories);

        return tourRepository.save(existingTour);
    }

    @Override
    public Tour getTour(Long id) {

        return tourRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tour not found with id : " + id));
    }

    @Override
    public List<Tour> getAllTours() {

        return tourRepository.findAll();
    }

    @Override
    public List<Tour> getTourByTourCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Tour code cannot be null or empty");
        }

        TourCode tourCode = null;

        tourCode = tourCode.valueOf(code.toUpperCase().trim());

        List<Tour> tours = tourRepository.findByTourCode(tourCode);

        if (tours.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No tours found with tour code: " + code);
        }
        return tours;
    }

    /* Tour All Details */
    @Override
    public TourDetailsResponse getTourDetails(Long id) {

        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tour not found with id : " + id));

        List<TourSchedule> schedules = tourScheduleRepository.findByTourTourId(id);

        List<Itinerary> itinerary = itineraryRepository.findByTour_TourIdOrderByDayNumberAsc(id);

        List<Review> reviews = reviewRepository.findByTourTourId(id);

        ReviewSummary summary = new ReviewSummary();

        summary.setAverageRating(
                reviewRepository.getAverageRating(id));

        summary.setTotalReviews(
                reviewRepository.countByTourTourId(id));

        TourDetailsResponse response = new TourDetailsResponse();

        response.setTour(tour);
        response.setSchedules(schedules);
        response.setItinerary(itinerary);
        response.setReviews(reviews);
        response.setReviewSummary(summary);

        return response;
    }

    @Override
    public void deleteTour(Long id) {

        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tour not found with id : " + id));

        tourRepository.delete(tour);
    }
}