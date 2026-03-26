package com.hoteleria.backend.billing.api;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.hoteleria.backend.billing.domain.InvoiceStatus;

public record InvoiceResponse(
        UUID id,
        UUID reservationId,
        String invoiceNumber,
        BigDecimal subtotal,
        BigDecimal taxRate,
        BigDecimal taxAmount,
        BigDecimal total,
        InvoiceStatus status,
        OffsetDateTime issuedAt,
        OffsetDateTime paidAt,
        OffsetDateTime createdAt
) {
}
