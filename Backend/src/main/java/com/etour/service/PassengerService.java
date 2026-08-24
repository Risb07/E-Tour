package com.etour.service;

import java.util.List;

import com.etour.dto.PassengerDto;

public interface PassengerService {
    PassengerDto addPassenger(PassengerDto dto);
    PassengerDto updatePassenger(Long passengerId, PassengerDto dto);
    List<PassengerDto> getPassengersForBooking(Long bookingId);
    void deletePassenger(Long passengerId);
}
