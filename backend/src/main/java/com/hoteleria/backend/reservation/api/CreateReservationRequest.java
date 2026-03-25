package com.hoteleria.backend.reservation.api;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CreateReservationRequest(
        @NotNull UUID hotelId,
        @NotNull UUID guestId,
        @NotNull LocalDate checkInDate,
        @NotNull LocalDate checkOutDate,
        @NotEmpty @Valid List<CreateReservationRoomRequest> rooms
) {
}
