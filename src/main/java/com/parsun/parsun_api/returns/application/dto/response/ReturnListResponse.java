package com.parsun.parsun_api.returns.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ReturnListResponse(
        UUID id,
        String returnNumber,
        UUID invoiceId,
        UUID customerId,
        String customerName,
        String returnType,
        String reason,
        BigDecimal total,
        int lineCount,
        LocalDateTime createdAt
) {}
