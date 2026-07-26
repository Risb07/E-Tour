package com.etour.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.etour.entity.Category;
import com.etour.entity.Tour;
import com.etour.exception.ResourceNotFoundException; // ✅ ADD THIS IMPORT!
import com.etour.repository.CategoryRepository;
import com.etour.repository.TourRepository;
import com.etour.service.TourService;

@Service
public class TourServiceImpl implements TourService {

    @Autowired
    private TourRepository tourRepository;

    @Autowired
    private CategoryRepository categoryRepository;

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
    public void deleteTour(Long id) {

        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tour not found with id : " + id));

        tourRepository.delete(tour);
    }
}