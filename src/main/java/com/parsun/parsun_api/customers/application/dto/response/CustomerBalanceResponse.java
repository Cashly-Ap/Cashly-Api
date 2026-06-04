package com.parsun.parsun_api.customers.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CustomerBalanceResponse(
    UUID customerId,
    BigDecimal totalBalance,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
