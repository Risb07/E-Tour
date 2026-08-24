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

import com.etour.entity.Sector;
import com.etour.entity.SubSector;
import com.etour.entity.TourProduct;
import com.etour.exception.ResourceNotFoundException;
import com.etour.repository.SectorRepository;
import com.etour.repository.SubSectorRepository;
import com.etour.repository.TourProductRepository;

import jakarta.validation.Valid;

/**
 * BRD 3.3 - Sub-Sector page: all sub-sectors of a sector, and the products of
 * a sub-sector (Product page, BRD 3.4). Read endpoints are public.
 */
@RestController
@RequestMapping("/api/sub-sectors")
public class SubSectorController {

    private final SubSectorRepository subSectorRepository;
    private final SectorRepository sectorRepository;
    private final TourProductRepository tourProductRepository;

    public SubSectorController(SubSectorRepository subSectorRepository,
            SectorRepository sectorRepository,
            TourProductRepository tourProductRepository) {
        this.subSectorRepository = subSectorRepository;
        this.sectorRepository = sectorRepository;
        this.tourProductRepository = tourProductRepository;
    }

    @GetMapping
    public ResponseEntity<List<SubSector>> getAllSubSectors(
            @RequestParam(required = false) Long sectorId,
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        if (includeInactive) {
            return ResponseEntity.ok(subSectorRepository.findAll());
        }
        if (sectorId != null) {
            return ResponseEntity.ok(subSectorRepository.findBySector_SectorIdAndActiveTrueOrderBySortOrderAsc(sectorId));
        }
        return ResponseEntity.ok(subSectorRepository.findByActiveTrueOrderBySortOrderAsc());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubSector> getSubSectorById(@PathVariable Long id) {
        SubSector subSector = subSectorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sub-sector not found with id : " + id));
        return ResponseEntity.ok(subSector);
    }

    @GetMapping("/{id}/products")
    public ResponseEntity<List<TourProduct>> getProducts(@PathVariable Long id) {
        if (!subSectorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Sub-sector not found with id : " + id);
        }
        return ResponseEntity.ok(tourProductRepository.findBySubSector_SubSectorIdAndActiveTrueOrderBySortOrderAsc(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<SubSector> createSubSector(@Valid @RequestBody SubSector subSector) {
        if (subSector.getSector() == null || subSector.getSector().getSectorId() == null) {
            throw new ResourceNotFoundException("Sector is required");
        }
        Sector sector = sectorRepository.findById(subSector.getSector().getSectorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sector not found with id : " + subSector.getSector().getSectorId()));
        subSector.setSector(sector);
        return new ResponseEntity<>(subSectorRepository.save(subSector), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<SubSector> updateSubSector(@PathVariable Long id, @Valid @RequestBody SubSector request) {
        SubSector existing = subSectorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sub-sector not found with id : " + id));
        if (request.getSector() != null && request.getSector().getSectorId() != null) {
            Sector sector = sectorRepository.findById(request.getSector().getSectorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sector not found"));
            existing.setSector(sector);
        }
        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        existing.setIconUrl(request.getIconUrl());
        existing.setImageUrl(request.getImageUrl());
        existing.setActive(request.getActive());
        existing.setSortOrder(request.getSortOrder());
        return ResponseEntity.ok(subSectorRepository.save(existing));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSubSector(@PathVariable Long id) {
        SubSector subSector = subSectorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sub-sector not found with id : " + id));
        subSectorRepository.delete(subSector);
        return ResponseEntity.ok("Sub-sector deleted successfully");
    }
}
