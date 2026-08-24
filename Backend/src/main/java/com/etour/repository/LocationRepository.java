package com.etour.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.etour.entity.Location;

public interface LocationRepository extends JpaRepository<Location, Long> {
}
