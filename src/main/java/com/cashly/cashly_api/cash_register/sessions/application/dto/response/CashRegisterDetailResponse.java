package com.cashly.cashly_api.cash_register.sessions.application.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record CashRegisterDetailResponse(
        UUID id,
        UUID paymentMethodId,
        String paymentMethodName,
        int transactionCount,
        BigDecimal expectedAmount
) {}
