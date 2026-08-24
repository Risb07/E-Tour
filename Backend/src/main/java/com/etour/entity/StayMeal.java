package com.etour.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "stay_meal")
public class StayMeal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stay_meal_id")
    private Long stayMealId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tour_id", nullable = false)
    @JsonIgnoreProperties({ "categories", "tourCosts", "hibernateLazyInitializer", "handler" })
    private Tour tour;

    @NotNull
    @Column(name = "day_number", nullable = false)
    private Integer dayNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @Column(name = "hotel_name", length = 150)
    private String hotelName;

    private Boolean breakfast = false;
    private Boolean lunch = false;
    private Boolean dinner = false;

    public Long getStayMealId() { return stayMealId; }
    public void setStayMealId(Long stayMealId) { this.stayMealId = stayMealId; }
    public Tour getTour() { return tour; }
    public void setTour(Tour tour) { this.tour = tour; }
    public Integer getDayNumber() { return dayNumber; }
    public void setDayNumber(Integer dayNumber) { this.dayNumber = dayNumber; }
    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }
    public String getHotelName() { return hotelName; }
    public void setHotelName(String hotelName) { this.hotelName = hotelName; }
    public Boolean getBreakfast() { return breakfast; }
    public void setBreakfast(Boolean breakfast) { this.breakfast = breakfast; }
    public Boolean getLunch() { return lunch; }
    public void setLunch(Boolean lunch) { this.lunch = lunch; }
    public Boolean getDinner() { return dinner; }
    public void setDinner(Boolean dinner) { this.dinner = dinner; }
}
