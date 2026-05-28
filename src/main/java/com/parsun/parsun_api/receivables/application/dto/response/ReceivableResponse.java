package com.parsun.parsun_api.receivables.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ReceivableResponse(
        UUID id,
        UUID invoiceId,
        String invoiceNumber,
        UUID customerId,
        String customerName,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal balance,
        String status,
        LocalDate dueDate,
        LocalDateTime createdAt
) {

    public record AgingBucket(
            String label,
            int minDays,
            int maxDays,
            BigDecimal totalAmount,
            long count,
            List<ReceivableResponse> receivables
    ) {}

    public record AgingReport(
            List<AgingBucket> buckets,
            LocalDate calculatedAt
    ) {}
}
