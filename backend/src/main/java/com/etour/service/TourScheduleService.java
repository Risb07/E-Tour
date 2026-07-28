package com.etour.service;

import java.util.List;

import com.etour.entity.TourSchedule;

public interface TourScheduleService {

    TourSchedule createSchedule(TourSchedule schedule);

    TourSchedule getSchedule(Long scheduleId);

    List<TourSchedule> getAllSchedules();

    List<TourSchedule> getSchedulesByTourId(Long tourId);

    TourSchedule updateSchedule(Long scheduleId, TourSchedule schedule);

    void deleteSchedule(Long scheduleId);
}