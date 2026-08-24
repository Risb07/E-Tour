package com.etour.service;

import java.util.List;

import com.etour.dto.LocationDto;

public interface LocationService {
    LocationDto create(LocationDto dto);
    LocationDto update(Long id, LocationDto dto);
    LocationDto getById(Long id);
    List<LocationDto> getAll();
    void delete(Long id);
}
