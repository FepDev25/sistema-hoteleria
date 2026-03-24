package com.hoteleria.backend.catalog.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record HotelRequest(
        @NotBlank String name,
        @NotBlank String address,
        @NotBlank String city,
        @NotBlank String country,
        String phone,
        @Email String email,
        @NotNull Integer starRating,
        String description
) {
}
