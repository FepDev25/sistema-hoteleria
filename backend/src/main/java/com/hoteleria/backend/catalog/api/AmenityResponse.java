package com.hoteleria.backend.catalog.api;

import java.util.UUID;

public record AmenityResponse(
        UUID id,
        String name,
        String icon,
        String category
) {
}
