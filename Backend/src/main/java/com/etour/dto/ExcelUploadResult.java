package com.etour.dto;

import java.util.List;

public class ExcelUploadResult {
    private Long batchId;
    private int totalRows;
    private int successRows;
    private int failedRows;
    private String status;
    private List<String> errors;

    public ExcelUploadResult() {}

    public ExcelUploadResult(Long batchId, int totalRows, int successRows, int failedRows, String status,
            List<String> errors) {
        this.batchId = batchId;
        this.totalRows = totalRows;
        this.successRows = successRows;
        this.failedRows = failedRows;
        this.status = status;
        this.errors = errors;
    }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public int getTotalRows() { return totalRows; }
    public void setTotalRows(int totalRows) { this.totalRows = totalRows; }
    public int getSuccessRows() { return successRows; }
    public void setSuccessRows(int successRows) { this.successRows = successRows; }
    public int getFailedRows() { return failedRows; }
    public void setFailedRows(int failedRows) { this.failedRows = failedRows; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }
}
