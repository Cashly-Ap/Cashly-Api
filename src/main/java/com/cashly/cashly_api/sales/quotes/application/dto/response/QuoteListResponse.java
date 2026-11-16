package com.cashly.cashly_api.sales.quotes.application.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record QuoteListResponse(UUID id, String quoteNumber, UUID customerId,
        String customerName, String customerRif, String status,
        LocalDate expiryDate, BigDecimal subtotal, UUID createdBy,
        String createdByName, Instant createdAt, Instant updatedAt) {
}
