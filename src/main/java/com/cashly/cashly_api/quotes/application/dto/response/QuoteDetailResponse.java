package com.cashly.cashly_api.quotes.application.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record QuoteDetailResponse(
        UUID id,
        String quoteNumber,
        UUID customerId,
        String customerName,
        String customerRif,
        UUID createdBy,
        String createdByName,
        String status,
        LocalDate expiryDate,
        String notes,
        BigDecimal subtotal,
        UUID orderId,
        String orderNumber,
        UUID invoiceId,
        String invoiceNumber,
        Instant cancelledAt,
        String cancelledReason,
        Instant createdAt,
        Instant updatedAt,
        List<QuoteLineDetailResponse> lines
) {}
