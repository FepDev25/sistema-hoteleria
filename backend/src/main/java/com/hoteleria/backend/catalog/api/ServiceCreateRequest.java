package com.hoteleria.backend.catalog.api;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ServiceCreateRequest(
        @NotNull UUID hotelId,
        @NotBlank String name,
        String description,
        @NotNull @DecimalMin("0.00") BigDecimal unitPrice,
        @NotBlank String category,
        Boolean isActive
) {
}
