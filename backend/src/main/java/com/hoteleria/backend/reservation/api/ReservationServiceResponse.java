package com.hoteleria.backend.reservation.api;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ReservationServiceResponse(
        UUID id,
        UUID reservationId,
        UUID serviceId,
        Short quantity,
        BigDecimal unitPriceSnapshot,
        OffsetDateTime requestedAt
) {
}
