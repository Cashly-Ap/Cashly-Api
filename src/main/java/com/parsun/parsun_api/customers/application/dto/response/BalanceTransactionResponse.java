package com.parsun.parsun_api.customers.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record BalanceTransactionResponse(
    UUID id,
    UUID customerId,
    String type,
    BigDecimal amount,
    String referenceType,
    UUID referenceId,
    String notes,
    UUID createdBy,
    LocalDateTime createdAt
) {}
