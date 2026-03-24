package com.hoteleria.backend.catalog.api;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record RoomTypeResponse(
        UUID id,
        UUID hotelId,
        String name,
        String description,
        Short maxCapacity,
        BigDecimal basePricePerNight,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        List<UUID> amenityIds
) {
}
