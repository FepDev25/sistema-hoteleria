package com.hoteleria.backend.catalog.api;

import java.time.OffsetDateTime;
import java.util.UUID;

public record HotelResponse(
        UUID id,
        String name,
        String address,
        String city,
        String country,
        String phone,
        String email,
        Integer starRating,
        String description,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        OffsetDateTime deletedAt
) {
}
