package com.etour.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.etour.entity.Tour;
import com.etour.entity.TourCost;
import com.etour.exception.ResourceNotFoundException;
import com.etour.repository.TourCostRepository;
import com.etour.repository.TourRepository;
import com.etour.service.TourCostService;

@Service
public class TourCostServiceImpl implements TourCostService {

    private final TourCostRepository tourCostRepository;
    private final TourRepository tourRepository;

    public TourCostServiceImpl(TourCostRepository tourCostRepository, TourRepository tourRepository) {
        this.tourCostRepository = tourCostRepository;
        this.tourRepository = tourRepository;
    }

    @Override
    public TourCost createCost(Long tourId, TourCost cost) {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found with id : " + tourId));
        cost.setTour(tour);
        if (cost.getStatus() == null) {
            cost.setStatus(1);
        }
        return tourCostRepository.save(cost);
    }

    @Override
    public TourCost getCost(Long costId) {
        return tourCostRepository.findById(costId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour cost not found with id : " + costId));
    }

    @Override
    public List<TourCost> getAllCosts() {
        return tourCostRepository.findAll();
    }

    @Override
    public List<TourCost> getCostsByTourId(Long tourId) {
        return tourCostRepository.findByTour_TourIdOrderByCostIdDesc(tourId);
    }

    @Override
    public TourCost updateCost(Long costId, Long tourId, TourCost request) {
        TourCost existing = tourCostRepository.findById(costId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour cost not found with id : " + costId));
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found with id : " + tourId));
        existing.setTour(tour);
        existing.setBasePrice(request.getBasePrice());
        existing.setSinglePersonCost(request.getSinglePersonCost());
        existing.setExtraPersonCost(request.getExtraPersonCost());
        existing.setChildWithBedCost(request.getChildWithBedCost());
        existing.setChildWithoutBedCost(request.getChildWithoutBedCost());
        existing.setValidFrom(request.getValidFrom());
        existing.setValidTo(request.getValidTo());
        existing.setStatus(request.getStatus());
        return tourCostRepository.save(existing);
    }

    @Override
    public void deleteCost(Long costId) {
        TourCost cost = tourCostRepository.findById(costId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour cost not found with id : " + costId));
        tourCostRepository.delete(cost);
    }
}
