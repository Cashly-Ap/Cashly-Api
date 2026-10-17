package com.cashly.cashly_api.finance.billing.debit_notes.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record DebitNoteResponse(
        UUID id,
        String noteNumber,
        UUID customerId,
        String customerName,
        String customerRif,
        UUID invoiceId,
        String invoiceNumber,
        BigDecimal amount,
        String reason,
        UUID createdBy,
        String createdByName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
