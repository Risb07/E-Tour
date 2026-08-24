package com.etour.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.etour.entity.AdBanner;

public interface AdBannerRepository extends JpaRepository<AdBanner, Long> {

    List<AdBanner> findByStatusTrueOrderByAdIdAsc();

    List<AdBanner> findByPositionAndStatusTrueOrderByAdIdAsc(String position);

    List<AdBanner> findAllByOrderByAdIdAsc();
}
