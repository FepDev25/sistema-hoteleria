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

import com.hoteleria.backend.catalog.api.RoomTypeCreateRequest;
import com.hoteleria.backend.catalog.api.RoomTypeResponse;
import com.hoteleria.backend.catalog.service.RoomTypeCatalogService;

import jakarta.validation.Valid;

// controlador de tipos de habitacion
@RestController
@RequestMapping("/api/v1/room-types")
public class RoomTypeController {

    private final RoomTypeCatalogService roomTypeCatalogService;

    public RoomTypeController(RoomTypeCatalogService roomTypeCatalogService) {
        this.roomTypeCatalogService = roomTypeCatalogService;
    }

    @GetMapping
    public List<RoomTypeResponse> getRoomTypes(@RequestParam(required = false) UUID hotelId) {
        return roomTypeCatalogService.getRoomTypes(hotelId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoomTypeResponse createRoomType(@Valid @RequestBody RoomTypeCreateRequest request) {
        return roomTypeCatalogService.createRoomType(request);
    }
}
