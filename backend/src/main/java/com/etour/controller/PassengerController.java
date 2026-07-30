package com.etour.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.etour.entity.Passenger;
import com.etour.service.PassengerService;

@RestController
@RequestMapping("/api/passenger")
public class PassengerController {

    @Autowired
    private PassengerService passengerService;

    @PostMapping
    public ResponseEntity<Passenger> addPassenger(@RequestBody Passenger passenger) {

        Passenger savedPassenger = passengerService.addPassenger(passenger);

        return new ResponseEntity<>(savedPassenger, HttpStatus.CREATED);
    }

    @GetMapping("/{passengerId}")
    public ResponseEntity<Passenger> getPassengerById(@PathVariable Long passengerId) {

        return ResponseEntity.ok(passengerService.getPassengerById(passengerId));
    }

    @GetMapping
    public ResponseEntity<List<Passenger>> getAllPassengers() {

        return ResponseEntity.ok(passengerService.getAllPassenger());
    }

    @PutMapping("/{passengerId}")
    public ResponseEntity<Passenger> updatePassenger(@PathVariable Long passengerId,
            @RequestBody Passenger passenger) {

        return ResponseEntity.ok(passengerService.updatePassenger(passengerId, passenger));
    }

    @DeleteMapping("/{passengerId}")
    public ResponseEntity<String> deletePassenger(@PathVariable Long passengerId) {

        passengerService.deletePassenger(passengerId);

        return ResponseEntity.ok("Passenger deleted successfully.");
    }
}