package com.hoteleria.backend.catalog.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hoteleria.backend.catalog.api.AmenityCreateRequest;
import com.hoteleria.backend.catalog.api.AmenityResponse;
import com.hoteleria.backend.catalog.domain.Amenity;
import com.hoteleria.backend.catalog.repository.AmenityRepository;

// servicio para manejar las amenidades
@Service
public class AmenityCatalogService {

    private final AmenityRepository amenityRepository;

    public AmenityCatalogService(AmenityRepository amenityRepository) {
        this.amenityRepository = amenityRepository;
    }

    // obtener todas las amenidades
    @Transactional(readOnly = true)
    public List<AmenityResponse> getAmenities() {
        return amenityRepository.findAll().stream().map(this::toResponse).toList();
    }

    // crear una nueva amenidad
    @Transactional
    public AmenityResponse createAmenity(AmenityCreateRequest request) {
        Amenity entity = new Amenity();
        entity.setName(request.name());
        entity.setIcon(request.icon());
        entity.setCategory(request.category());

        return toResponse(amenityRepository.save(entity));
    }

    // convertir una entidad de amenidad a su respuesta correspondiente
    private AmenityResponse toResponse(Amenity entity) {
        return new AmenityResponse(entity.getId(), entity.getName(), entity.getIcon(), entity.getCategory());
    }
}
