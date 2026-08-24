package com.etour.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.etour.dto.RoomChargeDto;
import com.etour.entity.RoomCharge;
import com.etour.entity.Tour;
import com.etour.enums.Occupancy;
import com.etour.exception.ResourceConflictException;
import com.etour.exception.ResourceNotFoundException;
import com.etour.repository.RoomChargeRepository;
import com.etour.repository.TourRepository;

import jakarta.validation.Valid;

/**
 * Room-sharing charges per tour (BRD 3.7 room options).
 *
 * Reads are public - the booking page needs the prices before anyone logs in.
 * Writes are ADMIN-only, matching every other pricing endpoint.
 */
@RestController
@RequestMapping("/api/room-charges")
public class RoomChargeController {

    private final RoomChargeRepository roomChargeRepository;
    private final TourRepository tourRepository;

    public RoomChargeController(RoomChargeRepository roomChargeRepository, TourRepository tourRepository) {
        this.roomChargeRepository = roomChargeRepository;
        this.tourRepository = tourRepository;
    }

    /** Public: bookable options for a tour. */
    @GetMapping("/tour/{tourId}")
    public ResponseEntity<List<RoomChargeDto>> getForTour(
            @PathVariable Long tourId,
            @RequestParam(defaultValue = "false") boolean includeInactive) {

        List<RoomCharge> charges = includeInactive
                ? roomChargeRepository.findByTour_TourIdOrderByOccupancyAsc(tourId)
                : roomChargeRepository.findByTour_TourIdAndActiveTrue(tourId);

        return ResponseEntity.ok(charges.stream().map(this::toDto).toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/tour/{tourId}")
    public ResponseEntity<RoomChargeDto> create(@PathVariable Long tourId,
            @Valid @RequestBody RoomChargeDto dto) {

        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found with id : " + tourId));

        Occupancy occupancy = Occupancy.valueOf(dto.getOccupancy());
        // One charge per occupancy per tour - the DB enforces this too, but a
        // clear 409 beats a constraint-violation 500.
        if (roomChargeRepository.existsByTour_TourIdAndOccupancy(tourId, occupancy)) {
            throw new ResourceConflictException(
                    "A charge for " + occupancy + " already exists on this tour - edit it instead");
        }

        RoomCharge entity = new RoomCharge();
        entity.setTour(tour);
        entity.setOccupancy(occupancy);
        entity.setCharge(dto.getCharge());
        entity.setDescription(dto.getDescription());
        entity.setActive(dto.getActive() == null || dto.getActive());

        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(roomChargeRepository.save(entity)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{roomChargeId}")
    public ResponseEntity<RoomChargeDto> update(@PathVariable Long roomChargeId,
            @Valid @RequestBody RoomChargeDto dto) {

        RoomCharge existing = roomChargeRepository.findById(roomChargeId)
                .orElseThrow(() -> new ResourceNotFoundException("Room charge not found with id : " + roomChargeId));

        // Occupancy is the identity of the row alongside the tour; changing it
        // would silently collide with another row, so only the price and
        // presentation fields are editable.
        existing.setCharge(dto.getCharge());
        existing.setDescription(dto.getDescription());
        if (dto.getActive() != null) {
            existing.setActive(dto.getActive());
        }

        return ResponseEntity.ok(toDto(roomChargeRepository.save(existing)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{roomChargeId}")
    public ResponseEntity<Void> delete(@PathVariable Long roomChargeId) {
        RoomCharge existing = roomChargeRepository.findById(roomChargeId)
                .orElseThrow(() -> new ResourceNotFoundException("Room charge not found with id : " + roomChargeId));
        roomChargeRepository.delete(existing);
        return ResponseEntity.noContent().build();
    }

    private RoomChargeDto toDto(RoomCharge c) {
        return new RoomChargeDto(c.getRoomChargeId(), c.getTour().getTourId(),
                c.getOccupancy().name(), c.getCharge(), c.getDescription(), c.getActive());
    }
}
