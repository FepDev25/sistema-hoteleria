package com.hoteleria.backend.reservation.api;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record CreateReservationRoomRequest(
        @NotNull UUID roomId,
        @NotNull Short adults,
        Short children
) {
}
