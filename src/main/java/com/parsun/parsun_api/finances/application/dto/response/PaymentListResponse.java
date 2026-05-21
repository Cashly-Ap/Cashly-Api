package com.parsun.parsun_api.finances.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentListResponse(
        UUID id,
        String paymentNumber,
        UUID customerId,
        String customerName,
        String type,
        BigDecimal amount,
        String paymentMethodName,
        String status,
        BigDecimal appliedAmount,
        LocalDate paymentDate,
        LocalDateTime createdAt
) {}
