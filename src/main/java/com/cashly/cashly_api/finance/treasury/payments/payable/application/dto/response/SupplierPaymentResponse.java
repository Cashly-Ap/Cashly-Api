package com.cashly.cashly_api.finance.treasury.payments.payable.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record SupplierPaymentResponse(UUID id, String paymentNumber,
        UUID supplierId, BigDecimal amount, BigDecimal appliedAmount,
        UUID paymentMethodId, String source, String referenceNumber,
        LocalDate paymentDate, String notes, String status,
        LocalDateTime createdAt) {
}
