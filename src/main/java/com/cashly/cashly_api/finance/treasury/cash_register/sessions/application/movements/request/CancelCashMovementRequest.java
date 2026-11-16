package com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.movements.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelCashMovementRequest(
        @NotBlank @Size(max = 500) String reason) {
}
