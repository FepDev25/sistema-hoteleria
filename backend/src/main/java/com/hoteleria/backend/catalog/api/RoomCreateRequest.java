package com.hoteleria.backend.catalog.api;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RoomCreateRequest(
        @NotNull UUID hotelId,
        @NotNull UUID roomTypeId,
        @NotBlank String roomNumber,
        @NotNull Short floor
) {
}
