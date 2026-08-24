package com.etour.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.etour.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByUser_UserId(Long userId);

    boolean existsByUser_UserId(Long userId);

    Optional<Customer> findByUser_Email(String email);
}
