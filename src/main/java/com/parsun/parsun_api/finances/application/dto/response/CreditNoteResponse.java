package com.parsun.parsun_api.finances.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreditNoteResponse(
        UUID id,
        String noteNumber,
        UUID customerId,
        String customerName,
        UUID invoiceId,
        String invoiceNumber,
        BigDecimal amount,
        String reason,
        String status,
        BigDecimal appliedAmount,
        UUID createdBy,
        String createdByName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
