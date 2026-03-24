package com.hoteleria.backend.catalog.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.hoteleria.backend.catalog.api.ServiceCreateRequest;
import com.hoteleria.backend.catalog.api.ServiceResponse;
import com.hoteleria.backend.catalog.service.ServiceCatalogService;

import jakarta.validation.Valid;

// controlador de catalogo de servicios
@RestController
@RequestMapping("/api/v1/services")
public class ServiceController {

    private final ServiceCatalogService serviceCatalogService;

    public ServiceController(ServiceCatalogService serviceCatalogService) {
        this.serviceCatalogService = serviceCatalogService;
    }

    @GetMapping
    public List<ServiceResponse> getServices(@RequestParam(required = false) UUID hotelId) {
        return serviceCatalogService.getServices(hotelId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceResponse createService(@Valid @RequestBody ServiceCreateRequest request) {
        return serviceCatalogService.createService(request);
    }
}
