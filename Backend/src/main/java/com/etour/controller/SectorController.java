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
 * BRD 3.2/3.3 - Home page sector icons and the Home -> Sector -> Sub-Sector
 * drill-down. Read endpoints are public; writes are admin-only.
 */
@RestController
@RequestMapping("/api/sectors")
public class SectorController {

    private final SectorRepository sectorRepository;
    private final SubSectorRepository subSectorRepository;
    private final TourProductRepository tourProductRepository;

    public SectorController(SectorRepository sectorRepository,
            SubSectorRepository subSectorRepository,
            TourProductRepository tourProductRepository) {
        this.sectorRepository = sectorRepository;
        this.subSectorRepository = subSectorRepository;
        this.tourProductRepository = tourProductRepository;
    }

    @GetMapping
    public ResponseEntity<List<Sector>> getAllSectors(
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        if (includeInactive) {
            return ResponseEntity.ok(sectorRepository.findAll());
        }
        return ResponseEntity.ok(sectorRepository.findByActiveTrueOrderBySortOrderAsc());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sector> getSectorById(@PathVariable Long id) {
        Sector sector = sectorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sector not found with id : " + id));
        return ResponseEntity.ok(sector);
    }

    @GetMapping("/{id}/sub-sectors")
    public ResponseEntity<List<SubSector>> getSubSectors(@PathVariable Long id) {
        if (!sectorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Sector not found with id : " + id);
        }
        return ResponseEntity.ok(subSectorRepository.findBySector_SectorIdAndActiveTrueOrderBySortOrderAsc(id));
    }

    @GetMapping("/{id}/products")
    public ResponseEntity<List<TourProduct>> getProducts(@PathVariable Long id) {
        if (!sectorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Sector not found with id : " + id);
        }
        return ResponseEntity.ok(tourProductRepository.findBySubSector_Sector_SectorIdAndActiveTrueOrderBySortOrderAsc(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Sector> createSector(@Valid @RequestBody Sector sector) {
        return new ResponseEntity<>(sectorRepository.save(sector), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Sector> updateSector(@PathVariable Long id, @Valid @RequestBody Sector request) {
        Sector existing = sectorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sector not found with id : " + id));
        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        existing.setIconUrl(request.getIconUrl());
        existing.setImageUrl(request.getImageUrl());
        existing.setActive(request.getActive());
        existing.setSortOrder(request.getSortOrder());
        existing.setTourCount(request.getTourCount());
        return ResponseEntity.ok(sectorRepository.save(existing));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSector(@PathVariable Long id) {
        Sector sector = sectorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sector not found with id : " + id));
        sectorRepository.delete(sector);
        return ResponseEntity.ok("Sector deleted successfully");
    }
}
