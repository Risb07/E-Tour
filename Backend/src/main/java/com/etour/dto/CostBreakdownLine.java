package com.etour.dto;

import java.math.BigDecimal;

/** One priced line in the BRD 3.7 cost breakdown (e.g. "Adults @9999 x 2"). */
public class CostBreakdownLine {

    private String label;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal lineTotal;

    public CostBreakdownLine() {
    }

    public CostBreakdownLine(String label, BigDecimal unitPrice, Integer quantity, BigDecimal lineTotal) {
        this.label = label;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.lineTotal = lineTotal;
    }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }
}
