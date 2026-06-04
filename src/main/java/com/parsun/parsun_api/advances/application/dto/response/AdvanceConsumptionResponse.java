package com.parsun.parsun_api.advances.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AdvanceConsumptionResponse(
        UUID id,
        UUID advanceId,
        UUID invoiceId,
        BigDecimal amount,
        UUID paymentId,
        LocalDateTime createdAt
) {}
