package com.hoteleria.backend.catalog.api;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.hoteleria.backend.catalog.domain.RoomStatus;

public record RoomResponse(
        UUID id,
        UUID hotelId,
        UUID roomTypeId,
        String roomNumber,
        Short floor,
        RoomStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        OffsetDateTime deletedAt
) {
}
