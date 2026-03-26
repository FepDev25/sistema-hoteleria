package com.hoteleria.backend.billing.domain;

import java.math.BigDecimal;
import java.util.UUID;

public record InvoiceItem(
        UUID sourceId,
        String itemType,
        short quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {
}
