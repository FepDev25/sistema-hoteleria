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

import com.hoteleria.backend.catalog.api.RoomCreateRequest;
import com.hoteleria.backend.catalog.api.RoomResponse;
import com.hoteleria.backend.catalog.service.RoomCatalogService;

import jakarta.validation.Valid;

// controlador para manejar las peticiones relacionadas con las habitaciones de los hoteles
@RestController
@RequestMapping("/api/v1/rooms")
public class RoomController {

    private final RoomCatalogService roomCatalogService;

    public RoomController(RoomCatalogService roomCatalogService) {
        this.roomCatalogService = roomCatalogService;
    }

    @GetMapping
    public List<RoomResponse> getRooms(@RequestParam(required = false) UUID hotelId) {
        return roomCatalogService.getRooms(hotelId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoomResponse createRoom(@Valid @RequestBody RoomCreateRequest request) {
        return roomCatalogService.createRoom(request);
    }
}
