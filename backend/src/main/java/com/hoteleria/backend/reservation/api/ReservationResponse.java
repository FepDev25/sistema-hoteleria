package com.hoteleria.backend.reservation.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.hoteleria.backend.reservation.domain.ReservationStatus;

public record ReservationResponse(
        UUID id,
        UUID hotelId,
        UUID guestId,
        ReservationStatus status,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        BigDecimal totalAmount,
        String cancellationReason,
        OffsetDateTime confirmedAt,
        OffsetDateTime cancelledAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        boolean requiresManualReassignment
) {
}
