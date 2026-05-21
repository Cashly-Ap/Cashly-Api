package com.parsun.parsun_api.finances.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PaymentDetailResponse(
        UUID id,
        String paymentNumber,
        UUID customerId,
        String customerName,
        String type,
        BigDecimal amount,
        UUID paymentMethodId,
        String paymentMethodName,
        String referenceNumber,
        LocalDate paymentDate,
        String notes,
        String status,
        BigDecimal appliedAmount,
        UUID createdBy,
        String createdByName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<AllocationResponse> allocations
) {
    public record AllocationResponse(
            UUID id,
            UUID invoiceId,
            BigDecimal allocatedAmount,
            LocalDateTime createdAt
    ) {}
}
