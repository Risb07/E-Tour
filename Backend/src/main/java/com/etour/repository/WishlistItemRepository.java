package com.etour.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.etour.entity.WishlistItem;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    /**
     * Fetch-joins tour so rendering a wishlist of N tours is one query rather
     * than 1 + N lazy loads (the tour title/price is always needed here).
     */
    @Query("select w from WishlistItem w join fetch w.tour where w.customer.customerId = :customerId order by w.createdAt desc")
    List<WishlistItem> findByCustomerWithTour(Long customerId);

    Optional<WishlistItem> findByCustomer_CustomerIdAndTour_TourId(Long customerId, Long tourId);

    boolean existsByCustomer_CustomerIdAndTour_TourId(Long customerId, Long tourId);

    void deleteByCustomer_CustomerIdAndTour_TourId(Long customerId, Long tourId);
}
