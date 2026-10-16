package com.cashly.cashly_api.payments.collection.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        String paymentNumber,
        UUID customerId,
        BigDecimal amount,
        UUID paymentMethodId,
        String referenceNumber,
        LocalDate paymentDate,
        String notes,
        String status,
        BigDecimal appliedAmount,
        UUID createdBy,
        String createdByName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String cancelledReason,
        UUID verifiedBy,
        String verifiedByName,
        LocalDateTime verifiedAt
) {}
