package com.etour.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.etour.entity.Cart;
import com.etour.enums.CartStatus;

public interface CartRepository extends JpaRepository<Cart, Long> {
    List<Cart> findByCustomer_CustomerIdAndStatus(Long customerId, CartStatus status);
    Optional<Cart> findByCartIdAndCustomer_CustomerId(Long cartId, Long customerId);
}
