package com.etour.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.etour.dto.PassengerDto;
import com.etour.service.PassengerService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/passengers")
public class PassengerController {

    private final PassengerService passengerService;

    public PassengerController(PassengerService passengerService) {
        this.passengerService = passengerService;
    }

    @PostMapping
    public ResponseEntity<PassengerDto> addPassenger(@Valid @RequestBody PassengerDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(passengerService.addPassenger(dto));
    }

    @PutMapping("/{passengerId}")
    public ResponseEntity<PassengerDto> updatePassenger(@PathVariable Long passengerId,
            @Valid @RequestBody PassengerDto dto) {
        return ResponseEntity.ok(passengerService.updatePassenger(passengerId, dto));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<PassengerDto>> getForBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(passengerService.getPassengersForBooking(bookingId));
    }

    @DeleteMapping("/{passengerId}")
    public ResponseEntity<Void> deletePassenger(@PathVariable Long passengerId) {
        passengerService.deletePassenger(passengerId);
        return ResponseEntity.noContent().build();
    }
}
