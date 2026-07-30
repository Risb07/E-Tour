package com.etour.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.etour.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

}
