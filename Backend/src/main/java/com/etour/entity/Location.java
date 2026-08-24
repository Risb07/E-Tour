package com.etour.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "location")
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "location_id")
    private Long locationId;

    @NotBlank(message = "Location name is required")
    @Column(name = "location_name", nullable = false, length = 150)
    private String locationName;

    @Column(name = "state_province", length = 100)
    private String stateProvince;

    @NotBlank(message = "Country is required")
    @Column(nullable = false, length = 100)
    private String country;

    @Size(min = 2, max = 2, message = "Country code must be a 2-letter ISO code")
    @Column(name = "country_code", length = 2)
    private String countryCode;

    @Column(nullable = false)
    private Boolean status = true;

    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }
    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }
    public String getStateProvince() { return stateProvince; }
    public void setStateProvince(String stateProvince) { this.stateProvince = stateProvince; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
    public Boolean getStatus() { return status; }
    public void setStatus(Boolean status) { this.status = status; }
}
