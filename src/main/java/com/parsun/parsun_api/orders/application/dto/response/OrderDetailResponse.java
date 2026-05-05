package com.parsun.parsun_api.orders.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderDetailResponse(
        UUID id,
        String orderNumber,
        UUID customerId,
        String customerName,
        String customerRif,
        UUID createdBy,
        String createdByName,
        String status,
        LocalDate deliveryDate,
        String notes,
        BigDecimal subtotal,
        UUID invoiceId,
        String invoiceNumber,
        LocalDateTime cancelledAt,
        String cancelledReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<OrderLineDetailResponse> lines
) {
    public record OrderLineDetailResponse(
            UUID id,
            UUID productId,
            String productName,
            String productCode,
            BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal
    ) {}
}
