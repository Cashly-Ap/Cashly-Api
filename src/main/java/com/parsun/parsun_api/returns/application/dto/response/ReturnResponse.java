package com.parsun.parsun_api.returns.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ReturnResponse(
        UUID id,
        String returnNumber,
        UUID invoiceId,
        String invoiceNumber,
        UUID customerId,
        String customerName,
        String customerRif,
        String returnType,
        String reason,
        BigDecimal total,
        UUID createdBy,
        String createdByName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<ReturnLineResponse> lines
) {
    public record ReturnLineResponse(
            UUID productId,
            BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal,
            List<AllocationResponse> allocations
    ) {
        public record AllocationResponse(
                UUID warehouseId,
                String warehouseName,
                BigDecimal allocated
        ) {}
    }
}
