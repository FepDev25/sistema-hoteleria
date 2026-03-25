package com.hoteleria.backend.reservation.api;

import java.math.BigDecimal;
import java.util.UUID;

public record ReservationRoomResponse(
        UUID id,
        UUID reservationId,
        UUID roomId,
        BigDecimal pricePerNight,
        Short adults,
        Short children
) {
}
