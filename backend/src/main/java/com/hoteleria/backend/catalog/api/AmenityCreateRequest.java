package com.hoteleria.backend.catalog.api;

import jakarta.validation.constraints.NotBlank;

public record AmenityCreateRequest(
        @NotBlank String name,
        String icon,
        @NotBlank String category
) {
}
