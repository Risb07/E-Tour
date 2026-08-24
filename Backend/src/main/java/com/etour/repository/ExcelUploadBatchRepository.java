package com.etour.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.etour.entity.ExcelUploadBatch;

public interface ExcelUploadBatchRepository extends JpaRepository<ExcelUploadBatch, Long> {
}
