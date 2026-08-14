package com.parsun.parsun_api.cashregister.application.dto;

import com.parsun.parsun_api.cashregister.domain.valueobjects.CashMovementDirection;
import com.parsun.parsun_api.cashregister.domain.valueobjects.CashMovementType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record CashMovementRequest(
        @NotNull CashMovementDirection direction,
        @NotNull CashMovementType type,
        @NotNull @Positive BigDecimal amount,
        UUID paymentMethodId,
        @NotBlank String description
) {}
