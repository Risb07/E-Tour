package com.etour.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.etour.entity.CrawlingText;

public interface CrawlingTextRepository extends JpaRepository<CrawlingText, Long> {

    List<CrawlingText> findByIsActiveTrueOrderBySortOrderAsc();

    List<CrawlingText> findAllByOrderBySortOrderAsc();
}
