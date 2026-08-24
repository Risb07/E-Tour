package com.etour.service;

import java.util.List;

import com.etour.dto.WishlistItemResponse;

public interface WishlistService {

    /** The authenticated customer's saved tours, newest first. */
    List<WishlistItemResponse> getMyWishlist();

    /** Idempotent - saving an already-saved tour returns the existing row. */
    WishlistItemResponse add(Long tourId);

    /** Idempotent - removing a tour that isn't saved is a no-op. */
    void remove(Long tourId);

    /** Lets the tour page render the correct heart state on load. */
    boolean isSaved(Long tourId);
}
