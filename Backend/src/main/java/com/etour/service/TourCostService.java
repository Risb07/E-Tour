package com.etour.service;

import java.util.List;

import com.etour.entity.TourCost;

public interface TourCostService {

    TourCost createCost(Long tourId, TourCost cost);

    TourCost getCost(Long costId);

    List<TourCost> getAllCosts();

    List<TourCost> getCostsByTourId(Long tourId);

    TourCost updateCost(Long costId, Long tourId, TourCost cost);

    void deleteCost(Long costId);
}
