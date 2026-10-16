package com.cashly.cashly_api.customers.balance.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CustomerBalanceResponse(
    UUID customerId,
    BigDecimal totalBalance,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
