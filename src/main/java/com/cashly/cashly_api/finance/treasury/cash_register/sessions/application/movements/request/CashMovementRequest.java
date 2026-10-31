package com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.movements.request;

import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.valueobjects.CashMovementDirection;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.valueobjects.CashMovementType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record CashMovementRequest(@NotNull CashMovementDirection direction,
        @NotNull CashMovementType type, @NotNull @Positive BigDecimal amount,
        UUID paymentMethodId, @NotBlank String description) {
}
