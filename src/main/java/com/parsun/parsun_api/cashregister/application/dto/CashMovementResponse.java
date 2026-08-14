package com.parsun.parsun_api.cashregister.application.dto;

import com.parsun.parsun_api.cashregister.domain.valueobjects.CashMovementDirection;
import com.parsun.parsun_api.cashregister.domain.valueobjects.CashMovementType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CashMovementResponse(
        UUID id,
        CashMovementDirection direction,
        CashMovementType type,
        BigDecimal amount,
        UUID paymentMethodId,
        String paymentMethodName,
        String description,
        String sourceType,
        UUID sourceId,
        UUID createdBy,
        String createdByName,
        LocalDateTime occurredAt
) {}
