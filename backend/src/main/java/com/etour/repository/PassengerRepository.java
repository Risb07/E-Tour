package com.etour.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.etour.entity.Passenger;

@Repository
public interface PassengerRepository extends JpaRepository<Passenger, Long> {

    boolean existsByIdProofNumber(String idProofNumber);

}