package com.parsun.parsun_api.advances.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AdvanceListResponse(
        UUID id,
        String advanceNumber,
        BigDecimal amount,
        BigDecimal remainingAmount,
        String status,
        String notes,
        LocalDateTime createdAt
) {}
