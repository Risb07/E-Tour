package com.etour.service;

import java.util.List;

import com.etour.entity.Passenger;

public interface PassengerService {

    Passenger addPassenger(Passenger passenger);

    Passenger getPassengerById(Long passengerId);

    List<Passenger> getAllPassenger();

    Passenger updatePassenger(Long passengerId, Passenger passenger);

    void deletePassenger(Long passengerId);

}