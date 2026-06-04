package com.parsun.parsun_api.advances.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AdvanceResponse(
        UUID id,
        String advanceNumber,
        UUID customerId,
        BigDecimal amount,
        BigDecimal remainingAmount,
        String status,
        String notes,
        UUID createdBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<AdvanceConsumptionResponse> consumptions
) {}
