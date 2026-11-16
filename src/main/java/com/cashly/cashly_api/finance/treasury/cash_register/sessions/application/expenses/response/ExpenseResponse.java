package com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.expenses.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ExpenseResponse(UUID id, UUID registerId, String registerStatus,
        LocalDateTime registerOpenedAt, LocalDate registerClosureDate,
        BigDecimal amount, String description, UUID paymentMethodId,
        String paymentMethodName, UUID createdBy, String createdByName,
        LocalDateTime occurredAt, boolean cancelled, String cancellationReason,
        LocalDateTime cancelledAt, UUID cancelledBy, String cancelledByName) {
}
