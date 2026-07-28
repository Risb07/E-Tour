package com.etour.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.etour.entity.Tour;
import com.etour.entity.TourSchedule;
import com.etour.repository.TourRepository;
import com.etour.repository.TourScheduleRepository;
import com.etour.service.TourScheduleService;

import jakarta.persistence.EntityNotFoundException;

@Service
public class TourScheduleServiceImpl implements TourScheduleService {

    @Autowired
    private TourScheduleRepository scheduleRepository;

    @Autowired
    private TourRepository tourRepository;

    @Override
    public TourSchedule createSchedule(TourSchedule schedule) {

        Long tourId = schedule.getTour().getTourId();

        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Tour not found with id: " + tourId));

        schedule.setTour(tour);

        return scheduleRepository.save(schedule);
    }

    @Override
    public TourSchedule getSchedule(Long scheduleId) {

        return scheduleRepository.findById(scheduleId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Schedule not found with id: " + scheduleId));
    }

    @Override
    public List<TourSchedule> getAllSchedules() {
        return scheduleRepository.findAll();
    }

    @Override
    public List<TourSchedule> getSchedulesByTourId(Long tourId) {
        return scheduleRepository.findByTourTourId(tourId);
    }

    @Override
    public TourSchedule updateSchedule(Long scheduleId, TourSchedule schedule) {

        TourSchedule existing = scheduleRepository.findById(scheduleId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Schedule not found with id: " + scheduleId));

        if (schedule.getTour() != null) {

            Long tourId = schedule.getTour().getTourId();

            Tour tour = tourRepository.findById(tourId)
                    .orElseThrow(() ->
                            new EntityNotFoundException("Tour not found with id: " + tourId));

            existing.setTour(tour);
        }

        existing.setDepartureDate(schedule.getDepartureDate());
        existing.setReturnDate(schedule.getReturnDate());
        existing.setAvailableSeats(schedule.getAvailableSeats());
        existing.setPrice(schedule.getPrice());

        return scheduleRepository.save(existing);
    }

    @Override
    public void deleteSchedule(Long scheduleId) {

        TourSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Schedule not found with id: " + scheduleId));

        scheduleRepository.delete(schedule);
    }
}