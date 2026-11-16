package com.cashly.cashly_api.finance.treasury.receivables.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PendingPaymentResponse(UUID id, String paymentNumber,
        UUID customerId, String customerName, BigDecimal amount,
        String paymentMethodName, String status, String source,
        LocalDate paymentDate, LocalDateTime createdAt, String invoiceNumber) {
}
