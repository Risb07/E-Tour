package com.etour.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.etour.entity.Passenger;
import com.etour.service.PassengerService;

import com.etour.exception.ResourceNotFoundException;

import com.etour.repository.PassengerRepository;

@Service
public class PassengerServiceImpl implements PassengerService {

    private final PassengerRepository passengerRepository;

    public PassengerServiceImpl(PassengerRepository passengerRepository) {
        this.passengerRepository = passengerRepository;
    }

    @Override
    public Passenger addPassenger(Passenger passenger) {

        if (passengerRepository.existsByIdProofNumber(passenger.getIdProofNumber())) {
            throw new RuntimeException("ID Proof Number already exists.");
        }

        return passengerRepository.save(passenger);
    }

    @Override
    public Passenger getPassengerById(Long passengerId) {

        return passengerRepository.findById(passengerId)
                .orElseThrow(() -> new ResourceNotFoundException("Passenger not found."));
    }

    @Override
    public List<Passenger> getAllPassenger() {

        return passengerRepository.findAll();
    }

    @Override
    public Passenger updatePassenger(Long passengerId, Passenger passenger) {

        Passenger existing = passengerRepository.findById(passengerId)
                .orElseThrow(() -> new RuntimeException("Passenger not found."));

        existing.setBookingId(passenger.getBookingId());
        existing.setFullName(passenger.getFullName());
        existing.setGender(passenger.getGender());
        existing.setDob(passenger.getDob());
        existing.setNationality(passenger.getNationality());
        existing.setIdProofType(passenger.getIdProofType());
        existing.setIdProofNumber(passenger.getIdProofNumber());

        return passengerRepository.save(existing);
    }

    @Override
    public void deletePassenger(Long passengerId) {

        Passenger passenger = passengerRepository.findById(passengerId)
                .orElseThrow(() -> new RuntimeException("Passenger not found."));

        passengerRepository.delete(passenger);
    }
}