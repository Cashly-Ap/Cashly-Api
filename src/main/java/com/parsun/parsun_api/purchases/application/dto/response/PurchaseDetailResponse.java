package com.parsun.parsun_api.purchases.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PurchaseDetailResponse(
        UUID id,
        String purchaseNumber,
        UUID supplierId,
        String supplierName,
        String supplierRif,
        UUID createdBy,
        String createdByName,
        String status,
        String notes,
        BigDecimal subtotal,
        LocalDateTime confirmedAt,
        LocalDateTime cancelledAt,
        String cancelledReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<LineDetailResponse> lines
) {
    public record LineDetailResponse(
            UUID id,
            UUID productId,
            String productName,
            String productCode,
            BigDecimal quantity,
            BigDecimal unitCost,
            BigDecimal lineTotal,
            List<AllocationDetail> warehouseAllocations
    ) {
        public record AllocationDetail(
                UUID warehouseId,
                String warehouseCode,
                String warehouseName,
                BigDecimal quantity
        ) {}
    }
}
