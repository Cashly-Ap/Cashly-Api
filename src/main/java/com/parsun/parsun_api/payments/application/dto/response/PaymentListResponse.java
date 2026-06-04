package com.parsun.parsun_api.payments.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentListResponse(
        UUID id,
        String paymentNumber,
        UUID customerId,
        BigDecimal amount,
        String referenceNumber,
        LocalDate paymentDate,
        String status,
        BigDecimal appliedAmount,
        LocalDateTime createdAt
) {}
