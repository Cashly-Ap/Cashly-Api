package com.cashly.cashly_api.finance.treasury.payments.payable.application.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateSupplierPaymentRequest(
        @NotNull(message = "Supplier is required") UUID supplierId,
        @NotNull(message = "Amount is required") @Positive(message = "Amount must be positive") BigDecimal amount,
        UUID paymentMethodId,
        String referenceNumber,
        LocalDate paymentDate,
        String notes,
        String status) {
}
