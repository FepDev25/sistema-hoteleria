package com.hoteleria.backend.catalog.api;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ServiceResponse(
        UUID id,
        UUID hotelId,
        String name,
        String description,
        BigDecimal unitPrice,
        String category,
        Boolean isActive,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
