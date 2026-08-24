package com.etour.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.etour.dto.WishlistItemResponse;
import com.etour.entity.Customer;
import com.etour.entity.Tour;
import com.etour.entity.WishlistItem;
import com.etour.exception.ResourceNotFoundException;
import com.etour.repository.TourRepository;
import com.etour.repository.WishlistItemRepository;
import com.etour.security.CurrentUserProvider;
import com.etour.service.WishlistService;

@Service
public class WishlistServiceImpl implements WishlistService {

    private final WishlistItemRepository wishlistRepository;
    private final TourRepository tourRepository;
    private final CurrentUserProvider currentUserProvider;

    public WishlistServiceImpl(WishlistItemRepository wishlistRepository, TourRepository tourRepository,
            CurrentUserProvider currentUserProvider) {
        this.wishlistRepository = wishlistRepository;
        this.tourRepository = tourRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    public List<WishlistItemResponse> getMyWishlist() {
        Customer customer = currentUserProvider.currentCustomer();
        // Single fetch-joined query - no N+1 when rendering the wishlist page.
        return wishlistRepository.findByCustomerWithTour(customer.getCustomerId()).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public WishlistItemResponse add(Long tourId) {
        Customer customer = currentUserProvider.currentCustomer();
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found"));

        // Idempotent: a double-click or a stale client shouldn't 409.
        return wishlistRepository
                .findByCustomer_CustomerIdAndTour_TourId(customer.getCustomerId(), tourId)
                .map(this::toDto)
                .orElseGet(() -> {
                    WishlistItem item = new WishlistItem();
                    item.setCustomer(customer);
                    item.setTour(tour);
                    return toDto(wishlistRepository.save(item));
                });
    }

    @Override
    @Transactional
    public void remove(Long tourId) {
        Customer customer = currentUserProvider.currentCustomer();
        // Scoped by customer id, so one customer can never delete another's
        // saved tour by guessing an id.
        wishlistRepository.deleteByCustomer_CustomerIdAndTour_TourId(customer.getCustomerId(), tourId);
    }

    @Override
    public boolean isSaved(Long tourId) {
        Customer customer = currentUserProvider.currentCustomer();
        return wishlistRepository.existsByCustomer_CustomerIdAndTour_TourId(customer.getCustomerId(), tourId);
    }

    private WishlistItemResponse toDto(WishlistItem item) {
        Tour tour = item.getTour();
        return new WishlistItemResponse(
                item.getWishlistItemId(),
                tour.getTourId(),
                tour.getTitle(),
                tour.getTourCode() == null ? null : tour.getTourCode().name(),
                tour.getDurationDays(),
                tour.getBasePrice(),
                item.getCreatedAt());
    }
}
