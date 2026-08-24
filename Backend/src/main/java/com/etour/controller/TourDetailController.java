package com.etour.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.etour.dto.TourDetailDtos.ItineraryDto;
import com.etour.dto.TourDetailDtos.JourneyDetailDto;
import com.etour.dto.TourDetailDtos.StayMealDto;
import com.etour.dto.TourDetailDtos.TourAddonDto;
import com.etour.dto.TourDetailDtos.TourContentDto;
import com.etour.dto.TourDetailDtos.TourMediaDto;
import com.etour.service.TourDetailService;

import jakarta.validation.Valid;

/**
 * Everything shown on the "Tour Page" tabs from the BRD (Journey, Stay &
 * Meals, Passport/Visa, Weather, Do's & Don'ts, Terms, Gallery/Video/Map,
 * Add-ons) lives under the owning tour's URL. Reads are public (a visitor
 * browsing a tour page shouldn't need to log in); writes are admin-only.
 */
@RestController
@RequestMapping("/api/tours/{tourId}")
public class TourDetailController {

    private final TourDetailService service;

    public TourDetailController(TourDetailService service) {
        this.service = service;
    }

    // ----- Journey -----
    @GetMapping("/journey")
    public ResponseEntity<List<JourneyDetailDto>> getJourney(@PathVariable Long tourId) {
        return ResponseEntity.ok(service.getJourney(tourId));
    }

    @PostMapping("/journey")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JourneyDetailDto> addJourneyLeg(@PathVariable Long tourId,
            @Valid @RequestBody JourneyDetailDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addJourneyLeg(tourId, dto));
    }

    // ----- Stay & Meals -----
    @GetMapping("/stay-meals")
    public ResponseEntity<List<StayMealDto>> getStayMeals(@PathVariable Long tourId) {
        return ResponseEntity.ok(service.getStayMeals(tourId));
    }

    @PostMapping("/stay-meals")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StayMealDto> addStayMeal(@PathVariable Long tourId, @Valid @RequestBody StayMealDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addStayMeal(tourId, dto));
    }

    @DeleteMapping("/stay-meals/{stayMealId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteStayMeal(@PathVariable Long tourId, @PathVariable Long stayMealId) {
        service.deleteStayMeal(tourId, stayMealId);
        return ResponseEntity.noContent().build();
    }

    // ----- Content tabs (Passport/Visa, Weather, Do's & Don'ts, Terms) -----
    @GetMapping("/content")
    public ResponseEntity<List<TourContentDto>> getContent(@PathVariable Long tourId) {
        return ResponseEntity.ok(service.getContent(tourId));
    }

    @PostMapping("/content")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TourContentDto> upsertContent(@PathVariable Long tourId,
            @Valid @RequestBody TourContentDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.upsertContent(tourId, dto));
    }

    @DeleteMapping("/content/{tourContentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteContent(@PathVariable Long tourId, @PathVariable Long tourContentId) {
        service.deleteContent(tourId, tourContentId);
        return ResponseEntity.noContent().build();
    }

    // ----- Media (Gallery / Video / Map / Brochure) -----
    @GetMapping("/media")
    public ResponseEntity<List<TourMediaDto>> getMedia(@PathVariable Long tourId) {
        return ResponseEntity.ok(service.getMedia(tourId));
    }

    @PostMapping("/media")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TourMediaDto> addMedia(@PathVariable Long tourId, @Valid @RequestBody TourMediaDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addMedia(tourId, dto));
    }

    @DeleteMapping("/media/{mediaId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMedia(@PathVariable Long tourId, @PathVariable Long mediaId) {
        service.deleteMedia(tourId, mediaId);
        return ResponseEntity.noContent().build();
    }

    // ----- Add-ons -----
    @GetMapping("/addons")
    public ResponseEntity<List<TourAddonDto>> getAddons(@PathVariable Long tourId) {
        return ResponseEntity.ok(service.getAddons(tourId));
    }

    @PostMapping("/addons")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TourAddonDto> addAddon(@PathVariable Long tourId, @Valid @RequestBody TourAddonDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addAddon(tourId, dto));
    }

    @PutMapping("/addons/{addonId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TourAddonDto> updateAddon(@PathVariable Long tourId, @PathVariable Long addonId,
            @Valid @RequestBody TourAddonDto dto) {
        return ResponseEntity.ok(service.updateAddon(tourId, addonId, dto));
    }

    @DeleteMapping("/addons/{addonId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAddon(@PathVariable Long tourId, @PathVariable Long addonId) {
        service.deleteAddon(tourId, addonId);
        return ResponseEntity.noContent().build();
    }

    // ----- Itinerary -----
    @GetMapping("/itinerary")
    public ResponseEntity<List<ItineraryDto>> getItinerary(@PathVariable Long tourId) {
        return ResponseEntity.ok(service.getItinerary(tourId));
    }

    @PostMapping("/itinerary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ItineraryDto> addItineraryDay(@PathVariable Long tourId,
            @Valid @RequestBody ItineraryDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addItineraryDay(tourId, dto));
    }

    @PutMapping("/itinerary/{itineraryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ItineraryDto> updateItineraryDay(@PathVariable Long tourId,
            @PathVariable Long itineraryId, @Valid @RequestBody ItineraryDto dto) {
        return ResponseEntity.ok(service.updateItineraryDay(tourId, itineraryId, dto));
    }

    @DeleteMapping("/itinerary/{itineraryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteItineraryDay(@PathVariable Long tourId, @PathVariable Long itineraryId) {
        service.deleteItineraryDay(tourId, itineraryId);
        return ResponseEntity.noContent().build();
    }
}
