package com.etour.mapper;

import com.etour.dto.LocationDto;
import com.etour.entity.Location;

public class LocationMapper {

    private LocationMapper() {}

    public static LocationDto toDto(Location e) {
        LocationDto d = new LocationDto();
        d.setLocationId(e.getLocationId());
        d.setLocationName(e.getLocationName());
        d.setStateProvince(e.getStateProvince());
        d.setCountry(e.getCountry());
        d.setCountryCode(e.getCountryCode());
        d.setStatus(e.getStatus());
        return d;
    }

    public static void copyToEntity(LocationDto d, Location e) {
        e.setLocationName(d.getLocationName());
        e.setStateProvince(d.getStateProvince());
        e.setCountry(d.getCountry());
        e.setCountryCode(d.getCountryCode());
        e.setStatus(d.getStatus() == null ? true : d.getStatus());
    }
}
