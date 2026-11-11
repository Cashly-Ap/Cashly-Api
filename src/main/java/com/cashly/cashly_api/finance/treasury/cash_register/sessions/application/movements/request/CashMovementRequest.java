package com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.movements.request;

import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.valueobjects.CashMovementDirection;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.valueobjects.CashMovementType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CashMovementRequest(@NotNull CashMovementDirection direction,
        @NotNull CashMovementType type, @NotNull @Positive BigDecimal amount,
        UUID paymentMethodId, @NotBlank String description,
        LocalDateTime occurredAt) {

    public CashMovementRequest(CashMovementDirection direction,
            CashMovementType type, BigDecimal amount, UUID paymentMethodId,
            String description) {
        this(direction, type, amount, paymentMethodId, description, null);
    }
}
