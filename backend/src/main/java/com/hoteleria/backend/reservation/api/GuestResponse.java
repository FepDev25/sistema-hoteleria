package com.hoteleria.backend.reservation.api;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.hoteleria.backend.reservation.domain.DocumentType;

public record GuestResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String phone,
        DocumentType documentType,
        String documentNumber,
        LocalDate dateOfBirth,
        String nationality,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
