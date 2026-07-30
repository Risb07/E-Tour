package com.etour.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "TOURCOST")
public class TourCost {

      @Id
      @GeneratedValue(strategy = GenerationType.IDENTITY)
      @Column(name = "cost_id")
      private Long costId;

      @ManyToOne
      @JoinColumn(name = "tour_id")
      private Tour tour;

      @Column(name = "base_price", nullable = false, precision = 12, scale = 2)
      private BigDecimal basePrice;

      @Column(name = "single_person_cost", precision = 12, scale = 2)
      private BigDecimal singlePersonCost;

      @Column(name = "extra_person_cost", precision = 12, scale = 2)
      private BigDecimal extraPersonCost;

      @Column(name = "child_with_bed_cost", precision = 12, scale = 2)
      private BigDecimal childWithBedCost;

      @Column(name = "child_without_bed_cost", precision = 12, scale = 2)
      private BigDecimal childWithoutBedCost;

      @Column(name = "valid_from", nullable = false)
      private LocalDate validFrom;

      @Column(name = "valid_to", nullable = false)
      private LocalDate validTo;

      @Column(name = "status", nullable = false)
      private Integer status = 1; // 1 = Active, 0 = Inactive

      // Default Constructor
      public TourCost() {
      }

      // Constructor with all fields
      public TourCost(Long costId, Tour tour, BigDecimal basePrice,
                  BigDecimal singlePersonCost,
                  BigDecimal extraPersonCost,
                  BigDecimal childWithBedCost,
                  BigDecimal childWithoutBedCost,
                  LocalDate validFrom,
                  LocalDate validTo,
                  Integer status) {

            this.costId = costId;
            this.tour = tour;
            this.basePrice = basePrice;
            this.singlePersonCost = singlePersonCost;
            this.extraPersonCost = extraPersonCost;
            this.childWithBedCost = childWithBedCost;
            this.childWithoutBedCost = childWithoutBedCost;
            this.validFrom = validFrom;
            this.validTo = validTo;
            this.status = status;
      }

      // Getters and Setters
      public Long getCostId() {
            return costId;
      }

      public void setCostId(Long costId) {
            this.costId = costId;
      }

      public Tour getTour() {
            return tour;
      }

      public void setTour(Tour tour) {
            this.tour = tour;
      }

      public BigDecimal getBasePrice() {
            return basePrice;
      }

      public void setBasePrice(BigDecimal basePrice) {
            this.basePrice = basePrice;
      }

      public BigDecimal getSinglePersonCost() {
            return singlePersonCost;
      }

      public void setSinglePersonCost(BigDecimal singlePersonCost) {
            this.singlePersonCost = singlePersonCost;
      }

      public BigDecimal getExtraPersonCost() {
            return extraPersonCost;
      }

      public void setExtraPersonCost(BigDecimal extraPersonCost) {
            this.extraPersonCost = extraPersonCost;
      }

      public BigDecimal getChildWithBedCost() {
            return childWithBedCost;
      }

      public void setChildWithBedCost(BigDecimal childWithBedCost) {
            this.childWithBedCost = childWithBedCost;
      }

      public BigDecimal getChildWithoutBedCost() {
            return childWithoutBedCost;
      }

      public void setChildWithoutBedCost(BigDecimal childWithoutBedCost) {
            this.childWithoutBedCost = childWithoutBedCost;
      }

      public LocalDate getValidFrom() {
            return validFrom;
      }

      public void setValidFrom(LocalDate validFrom) {
            this.validFrom = validFrom;
      }

      public LocalDate getValidTo() {
            return validTo;
      }

      public void setValidTo(LocalDate validTo) {
            this.validTo = validTo;
      }

      public Integer getStatus() {
            return status;
      }

      public void setStatus(Integer status) {
            this.status = status;
      }

      // toString method
      @Override
      public String toString() {
            return "TourCost{" +
                        "costId=" + costId +
                        ", tourId=" + (tour != null ? tour.getTourId() : null) +
                        ", basePrice=" + basePrice +
                        ", singlePersonCost=" + singlePersonCost +
                        ", extraPersonCost=" + extraPersonCost +
                        ", childWithBedCost=" + childWithBedCost +
                        ", childWithoutBedCost=" + childWithoutBedCost +
                        ", validFrom=" + validFrom +
                        ", validTo=" + validTo +
                        ", status=" + status +
                        '}';
      }

      // equals and hashCode
      @Override
      public boolean equals(Object o) {
            if (this == o)
                  return true;
            if (o == null || getClass() != o.getClass())
                  return false;
            TourCost tourCost = (TourCost) o;
            return costId != null && costId.equals(tourCost.costId);
      }

      @Override
      public int hashCode() {
            return getClass().hashCode();
      }
}