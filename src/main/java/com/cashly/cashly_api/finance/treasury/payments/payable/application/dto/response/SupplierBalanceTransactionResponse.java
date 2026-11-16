package com.cashly.cashly_api.finance.treasury.payments.payable.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record SupplierBalanceTransactionResponse(UUID id, UUID supplierId,
        String type, BigDecimal amount, String referenceType, UUID referenceId,
        String number, String referenceNumber, UUID createdBy,
        LocalDateTime createdAt) {
}
