package com.cashly.cashly_api.cash_register.sessions.application.movements.response;

import com.cashly.cashly_api.cash_register.sessions.domain.valueobjects.CashMovementDirection;
import com.cashly.cashly_api.cash_register.sessions.domain.valueobjects.CashMovementType;

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
