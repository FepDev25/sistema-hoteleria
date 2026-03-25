package com.hoteleria.backend.reservation.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.hoteleria.backend.reservation.api.GuestRequest;
import com.hoteleria.backend.reservation.api.GuestResponse;
import com.hoteleria.backend.reservation.service.GuestService;

import jakarta.validation.Valid;

// controlador para gestion de huespedes
@RestController
@RequestMapping("/api/v1/guests")
public class GuestController {

    private final GuestService guestService;

    public GuestController(GuestService guestService) {
        this.guestService = guestService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GuestResponse createGuest(@Valid @RequestBody GuestRequest request) {
        return guestService.createGuest(request);
    }
}
