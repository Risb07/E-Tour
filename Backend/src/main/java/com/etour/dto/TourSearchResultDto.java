package com.etour.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One row of BRD 3.6 search results. Carries everything the results table has
 * to show - tour code, start/end date, duration, cost, rating, image - so the
 * frontend renders a page of results from a single response instead of firing
 * a schedule/media/rating request per row.
 */
public class TourSearchResultDto {

    private Long tourId;
    private String title;
    private String description;
    private String tourCode;
    private Integer durationDays;
    private BigDecimal basePrice;
    private String imageUrl;

    // Nearest departure matching the search window (null if the tour has no
    // matching schedule - e.g. when searching without a date filter and the
    // tour has no upcoming departures loaded yet).
    private Long scheduleId;
    private LocalDate departureDate;
    private LocalDate returnDate;
    private Integer availableSeats;
    private BigDecimal schedulePrice;

    private Double averageRating;
    private Long totalReviews;

    public TourSearchResultDto() {
    }

    public Long getTourId() { return tourId; }
    public void setTourId(Long tourId) { this.tourId = tourId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getTourCode() { return tourCode; }
    public void setTourCode(String tourCode) { this.tourCode = tourCode; }
    public Integer getDurationDays() { return durationDays; }
    public void setDurationDays(Integer durationDays) { this.durationDays = durationDays; }
    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }
    public LocalDate getDepartureDate() { return departureDate; }
    public void setDepartureDate(LocalDate departureDate) { this.departureDate = departureDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
    public Integer getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(Integer availableSeats) { this.availableSeats = availableSeats; }
    public BigDecimal getSchedulePrice() { return schedulePrice; }
    public void setSchedulePrice(BigDecimal schedulePrice) { this.schedulePrice = schedulePrice; }
    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }
    public Long getTotalReviews() { return totalReviews; }
    public void setTotalReviews(Long totalReviews) { this.totalReviews = totalReviews; }
}
