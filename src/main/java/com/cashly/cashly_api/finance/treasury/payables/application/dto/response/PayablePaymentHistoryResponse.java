package com.cashly.cashly_api.finance.treasury.payables.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PayablePaymentHistoryResponse(UUID id, String paymentNumber,
        UUID supplierId, String supplierName, BigDecimal amount,
        BigDecimal appliedAmount, String paymentMethodName, String status,
        String source, String referenceNumber, LocalDate paymentDate,
        String notes, LocalDateTime createdAt, String createdByName,
        String purchaseNumber) {
}
