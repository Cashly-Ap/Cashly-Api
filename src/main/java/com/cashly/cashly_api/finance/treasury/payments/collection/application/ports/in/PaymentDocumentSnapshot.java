package com.cashly.cashly_api.finance.treasury.payments.collection.application.ports.in;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentDocumentSnapshot(UUID id, String paymentNumber, BigDecimal amount, String referenceNumber,
        String notes, String status, LocalDateTime createdAt) {}
