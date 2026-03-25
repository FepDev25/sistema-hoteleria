package com.hoteleria.backend.reservation.api;

import java.time.LocalDate;

import com.hoteleria.backend.reservation.domain.DocumentType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GuestRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank @Email String email,
        String phone,
        @NotNull DocumentType documentType,
        @NotBlank String documentNumber,
        @NotNull LocalDate dateOfBirth,
        String nationality
) {
}
