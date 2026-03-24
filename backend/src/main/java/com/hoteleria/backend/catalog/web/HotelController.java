package com.hoteleria.backend.catalog.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.hoteleria.backend.catalog.api.HotelRequest;
import com.hoteleria.backend.catalog.api.HotelResponse;
import com.hoteleria.backend.catalog.service.HotelCatalogService;

import jakarta.validation.Valid;

// controlador para manejar las peticiones relacionadas con los hoteles
@RestController
@RequestMapping("/api/v1/hotels")
public class HotelController {

    private final HotelCatalogService hotelCatalogService;

    public HotelController(HotelCatalogService hotelCatalogService) {
        this.hotelCatalogService = hotelCatalogService;
    }

    @GetMapping
    public List<HotelResponse> getHotels() {
        return hotelCatalogService.getHotels();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HotelResponse createHotel(@Valid @RequestBody HotelRequest request) {
        return hotelCatalogService.createHotel(request);
    }
}
