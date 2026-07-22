package com.example.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.entity.Tour;
import com.example.repository.TourRepository;

@Service
public class TourServiceImpl implements TourService{
	
	
	@Autowired
	private TourRepository repository;

	@Override
	public Tour createTour(Tour tour) {
	    return repository.save(tour);
	}

	@Override
	public Tour updateTour(int id, Tour tour) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Tour getTour(int id) {
	    return repository.findById(id)
	            .orElseThrow(() -> new RuntimeException("Tour not found"));
	}

	@Override
	public List<Tour> getAllTours() {
	    return repository.findAll();
	}

	@Override
	public void deleteTour(int id) {
		// TODO Auto-generated method stub
		
	}



}
