package com.hoteleria.backend.billing.api;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record CreateInvoiceRequest(
        @NotNull UUID reservationId
) {
}
