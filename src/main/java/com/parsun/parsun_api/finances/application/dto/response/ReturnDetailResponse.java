package com.parsun.parsun_api.finances.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ReturnDetailResponse(
        UUID id,
        String returnNumber,
        UUID invoiceId,
        String invoiceNumber,
        UUID customerId,
        String customerName,
        String returnType,
        String reason,
        String status,
        UUID createdBy,
        String createdByName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<ReturnLineResponse> lines
) {
    public record ReturnLineResponse(
            UUID id,
            UUID invoiceLineId,
            UUID productId,
            String productName,
            BigDecimal quantity,
            UUID warehouseId,
            String warehouseName,
            BigDecimal unitPrice,
            BigDecimal lineTotal
    ) {}
}
