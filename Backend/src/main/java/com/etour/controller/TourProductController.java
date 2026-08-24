package com.etour.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.etour.entity.SubSector;
import com.etour.entity.Tour;
import com.etour.entity.TourProduct;
import com.etour.exception.ResourceNotFoundException;
import com.etour.repository.SubSectorRepository;
import com.etour.repository.TourProductRepository;
import com.etour.repository.TourRepository;

import jakarta.validation.Valid;

/**
 * BRD 3.4 - Product page: products of a sub-sector, linking on to the Tour
 * page. Read endpoints are public.
 */
@RestController
@RequestMapping("/api/products")
public class TourProductController {

    private final TourProductRepository tourProductRepository;
    private final SubSectorRepository subSectorRepository;
    private final TourRepository tourRepository;

    public TourProductController(TourProductRepository tourProductRepository,
            SubSectorRepository subSectorRepository, TourRepository tourRepository) {
        this.tourProductRepository = tourProductRepository;
        this.subSectorRepository = subSectorRepository;
        this.tourRepository = tourRepository;
    }

    @GetMapping
    public ResponseEntity<List<TourProduct>> getAllProducts(
            @RequestParam(required = false) Long subSectorId,
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        if (includeInactive) {
            return ResponseEntity.ok(tourProductRepository.findAll());
        }
        if (subSectorId != null) {
            return ResponseEntity.ok(tourProductRepository.findBySubSector_SubSectorIdAndActiveTrueOrderBySortOrderAsc(subSectorId));
        }
        return ResponseEntity.ok(tourProductRepository.findByActiveTrueOrderBySortOrderAsc());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TourProduct> getProductById(@PathVariable Long id) {
        TourProduct product = tourProductRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id : " + id));
        return ResponseEntity.ok(product);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<TourProduct> createProduct(@Valid @RequestBody TourProduct product) {
        if (product.getSubSector() == null || product.getSubSector().getSubSectorId() == null) {
            throw new ResourceNotFoundException("Sub-sector is required");
        }
        SubSector subSector = subSectorRepository.findById(product.getSubSector().getSubSectorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sub-sector not found with id : " + product.getSubSector().getSubSectorId()));
        product.setSubSector(subSector);
        product.setTour(resolveTour(product.getTour()));
        return new ResponseEntity<>(tourProductRepository.save(product), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<TourProduct> updateProduct(@PathVariable Long id, @Valid @RequestBody TourProduct request) {
        TourProduct existing = tourProductRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id : " + id));
        if (request.getSubSector() != null && request.getSubSector().getSubSectorId() != null) {
            SubSector subSector = subSectorRepository.findById(request.getSubSector().getSubSectorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sub-sector not found"));
            existing.setSubSector(subSector);
        }
        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        existing.setImageUrl(request.getImageUrl());
        existing.setBaseCost(request.getBaseCost());
        existing.setDurationDays(request.getDurationDays());
        existing.setDurationNights(request.getDurationNights());
        existing.setTourCode(request.getTourCode());
        existing.setStartDate(request.getStartDate());
        existing.setEndDate(request.getEndDate());
        existing.setActive(request.getActive());
        existing.setSortOrder(request.getSortOrder());
        existing.setTour(resolveTour(request.getTour()));
        return ResponseEntity.ok(tourProductRepository.save(existing));
    }

    // Resolves a client-supplied { tourId } stub to a managed Tour, or null
    // to unlink. A product doesn't have to be linked to a bookable Tour -
    // the frontend shows "coming soon" for unlinked ones (see BRD 3.4 gap
    // noted during integration: this catalog didn't originally reference the
    // bookable Tour/TourSchedule model at all).
    private Tour resolveTour(Tour requested) {
        if (requested == null || requested.getTourId() == null) {
            return null;
        }
        return tourRepository.findById(requested.getTourId())
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found with id : " + requested.getTourId()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        TourProduct product = tourProductRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id : " + id));
        tourProductRepository.delete(product);
        return ResponseEntity.ok("Product deleted successfully");
    }
}
