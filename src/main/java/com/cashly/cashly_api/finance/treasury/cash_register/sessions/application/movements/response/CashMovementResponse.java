package com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.movements.response;

import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.valueobjects.CashMovementDirection;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.valueobjects.CashMovementType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CashMovementResponse(UUID id, CashMovementDirection direction,
        CashMovementType type, BigDecimal amount, UUID paymentMethodId,
        String paymentMethodName, String description, String sourceType,
        UUID sourceId, UUID createdBy, String createdByName,
        LocalDateTime occurredAt, boolean cancelled, String cancellationReason,
        LocalDateTime cancelledAt, UUID cancelledBy, String cancelledByName) {

    public CashMovementResponse(UUID id, CashMovementDirection direction,
            CashMovementType type, BigDecimal amount, UUID paymentMethodId,
            String paymentMethodName, String description, String sourceType,
            UUID sourceId, UUID createdBy, String createdByName,
            LocalDateTime occurredAt) {
        this(id, direction, type, amount, paymentMethodId, paymentMethodName,
                description, sourceType, sourceId, createdBy, createdByName,
                occurredAt, false, null, null, null, null);
    }

    public static CashMovementResponse from(
            com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.entity.CashMovement movement) {
        return new CashMovementResponse(movement.getId(),
                movement.getDirection(), movement.getType(),
                movement.getAmount(), movement.getPaymentMethodId(),
                movement.getPaymentMethodName(), movement.getDescription(),
                movement.getSourceType(), movement.getSourceId(),
                movement.getCreatedBy(), movement.getCreatedByName(),
                movement.getOccurredAt(), movement.isCancelled(),
                movement.getCancellationReason(), movement.getCancelledAt(),
                movement.getCancelledBy(), movement.getCancelledByName());
    }
}
