package com.etour.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.etour.dto.WishlistItemResponse;
import com.etour.service.WishlistService;

/**
 * Wishlist is per-customer and always resolved from the JWT principal - there
 * is deliberately no customerId in any path here.
 */
@RestController
@RequestMapping("/api/wishlist")
@PreAuthorize("hasRole('CUSTOMER')")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping
    public ResponseEntity<List<WishlistItemResponse>> getMyWishlist() {
        return ResponseEntity.ok(wishlistService.getMyWishlist());
    }

    @GetMapping("/tour/{tourId}")
    public ResponseEntity<Map<String, Boolean>> isSaved(@PathVariable Long tourId) {
        return ResponseEntity.ok(Map.of("saved", wishlistService.isSaved(tourId)));
    }

    @PostMapping("/tour/{tourId}")
    public ResponseEntity<WishlistItemResponse> add(@PathVariable Long tourId) {
        return ResponseEntity.ok(wishlistService.add(tourId));
    }

    @DeleteMapping("/tour/{tourId}")
    public ResponseEntity<Void> remove(@PathVariable Long tourId) {
        wishlistService.remove(tourId);
        return ResponseEntity.noContent().build();
    }
}
