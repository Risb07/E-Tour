package com.etour.service;

import java.util.List;

import com.etour.entity.Tour;

public interface TourService {

    Tour createTour(Tour tour);

    Tour updateTour(Long id, Tour tour);

    Tour getTour(Long id);

    List<Tour> getAllTours();

    void deleteTour(Long id);
}