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

import com.etour.entity.AdBanner;
import com.etour.exception.ResourceNotFoundException;
import com.etour.repository.AdBannerRepository;

import jakarta.validation.Valid;

/**
 * BRD 3.1 - Showcase page advertisement banners. All banner images/A-V are
 * stored in the database (image_url/media), never hard-coded. Read endpoints
 * are public.
 */
@RestController
@RequestMapping("/api/ad-banners")
public class AdBannerController {

    private final AdBannerRepository adBannerRepository;

    public AdBannerController(AdBannerRepository adBannerRepository) {
        this.adBannerRepository = adBannerRepository;
    }

    @GetMapping
    public ResponseEntity<List<AdBanner>> getActiveBanners(
            @RequestParam(required = false) String position,
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        if (includeInactive) {
            return ResponseEntity.ok(adBannerRepository.findAll());
        }
        if (position != null && !position.isBlank()) {
            return ResponseEntity.ok(adBannerRepository.findByPositionAndStatusTrueOrderByAdIdAsc(position));
        }
        return ResponseEntity.ok(adBannerRepository.findByStatusTrueOrderByAdIdAsc());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdBanner> getBannerById(@PathVariable Long id) {
        AdBanner banner = adBannerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ad banner not found with id : " + id));
        return ResponseEntity.ok(banner);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<AdBanner> createBanner(@Valid @RequestBody AdBanner banner) {
        return new ResponseEntity<>(adBannerRepository.save(banner), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<AdBanner> updateBanner(@PathVariable Long id, @Valid @RequestBody AdBanner request) {
        AdBanner existing = adBannerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ad banner not found with id : " + id));
        existing.setTitle(request.getTitle());
        existing.setImageUrl(request.getImageUrl());
        existing.setLinkUrl(request.getLinkUrl());
        existing.setPosition(request.getPosition());
        existing.setStatus(request.getStatus());
        return ResponseEntity.ok(adBannerRepository.save(existing));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBanner(@PathVariable Long id) {
        AdBanner banner = adBannerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ad banner not found with id : " + id));
        adBannerRepository.delete(banner);
        return ResponseEntity.ok("Ad banner deleted successfully");
    }
}
