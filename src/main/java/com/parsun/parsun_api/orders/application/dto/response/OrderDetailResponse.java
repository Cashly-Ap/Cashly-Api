package com.parsun.parsun_api.orders.application.dto.response;

import java.math.BigDecimal;
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
        String notes,
        BigDecimal subtotal,
        UUID invoiceId,
        String invoiceNumber,
        LocalDateTime cancelledAt,
        String cancelledReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<OrderLineDetailResponse> lines,
        BigDecimal creditLimit,
        BigDecimal availableCredit
) {
    public record OrderLineDetailResponse(
            UUID id,
            UUID productId,
            String productName,
            String productCode,
            BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal,
            List<AllocationDetail> warehouseAllocations,
            List<WarehouseStockDetail> warehouseStocks
    ) {
        public record AllocationDetail(
                UUID warehouseId,
                String warehouseCode,
                String warehouseName,
                BigDecimal allocated,
                int availableStock
        ) {}
    }

    public record WarehouseStockDetail(
            UUID warehouseId,
            String warehouseCode,
            String warehouseName,
            int quantity,
            int reserved,
            int available
    ) {}
}
