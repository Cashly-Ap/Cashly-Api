package com.cashly.cashly_api.finance.treasury.payments.reporting.application.dto.model;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentReportData(UUID id, String paymentNumber, BigDecimal amount, String referenceNumber,
        String notes, String status, LocalDateTime createdAt) {}
