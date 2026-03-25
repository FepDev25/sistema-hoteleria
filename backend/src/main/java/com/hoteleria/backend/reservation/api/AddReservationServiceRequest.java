package com.hoteleria.backend.reservation.api;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddReservationServiceRequest(
        @NotNull UUID serviceId,
        @NotNull @Min(1) Short quantity
) {
}
