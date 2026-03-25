package com.hoteleria.backend.reservation.api;

import jakarta.validation.constraints.NotBlank;

public record CancelReservationRequest(
        @NotBlank String cancellationReason
) {
}
