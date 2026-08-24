package com.etour.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.etour.dto.LocationDto;
import com.etour.entity.Location;
import com.etour.exception.ResourceNotFoundException;
import com.etour.mapper.LocationMapper;
import com.etour.repository.LocationRepository;
import com.etour.service.LocationService;

@Service
public class LocationServiceImpl implements LocationService {

    private final LocationRepository repository;

    public LocationServiceImpl(LocationRepository repository) {
        this.repository = repository;
    }

    @Override
    public LocationDto create(LocationDto dto) {
        Location entity = new Location();
        LocationMapper.copyToEntity(dto, entity);
        return LocationMapper.toDto(repository.save(entity));
    }

    @Override
    public LocationDto update(Long id, LocationDto dto) {
        Location entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found: " + id));
        LocationMapper.copyToEntity(dto, entity);
        return LocationMapper.toDto(repository.save(entity));
    }

    @Override
    public LocationDto getById(Long id) {
        return LocationMapper.toDto(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found: " + id)));
    }

    @Override
    public List<LocationDto> getAll() {
        return repository.findAll().stream().map(LocationMapper::toDto).toList();
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Location not found: " + id);
        }
        repository.deleteById(id);
    }
}
