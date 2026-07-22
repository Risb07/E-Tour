package com.example.service;

import java.util.List;

import com.example.entity.Tour;

public interface TourService {

	Tour createTour(Tour tour);

    Tour updateTour(int id, Tour tour);

    Tour getTour(int id);

    List<Tour> getAllTours();

    void deleteTour(int id);
    
}

