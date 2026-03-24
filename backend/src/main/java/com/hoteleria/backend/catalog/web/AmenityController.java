package com.hoteleria.backend.catalog.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.hoteleria.backend.catalog.api.AmenityCreateRequest;
import com.hoteleria.backend.catalog.api.AmenityResponse;
import com.hoteleria.backend.catalog.service.AmenityCatalogService;

import jakarta.validation.Valid;

// controlador para manejar las peticiones relacionadas con las amenidades
@RestController
@RequestMapping("/api/v1/amenities")
public class AmenityController {

    private final AmenityCatalogService amenityCatalogService;

    public AmenityController(AmenityCatalogService amenityCatalogService) {
        this.amenityCatalogService = amenityCatalogService;
    }

    @GetMapping
    public List<AmenityResponse> getAmenities() {
        return amenityCatalogService.getAmenities();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AmenityResponse createAmenity(@Valid @RequestBody AmenityCreateRequest request) {
        return amenityCatalogService.createAmenity(request);
    }
}
