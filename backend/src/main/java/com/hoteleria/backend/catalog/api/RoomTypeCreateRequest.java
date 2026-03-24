package com.hoteleria.backend.catalog.api;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RoomTypeCreateRequest(
        @NotNull UUID hotelId,
        @NotBlank String name,
        String description,
        @NotNull Short maxCapacity,
        // se valida que el precio base por noche sea al menos 0.01
        @NotNull @DecimalMin("0.01") BigDecimal basePricePerNight,
        java.util.List<UUID> amenityIds
) {
}
