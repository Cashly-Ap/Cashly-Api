package com.parsun.parsun_api.cashregister.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CashRegisterDetailResponse(
        UUID id,
        UUID paymentMethodId,
        String paymentMethodName,
        int transactionCount,
        BigDecimal expectedAmount
) {}
