package com.cashly.cashly_api.procurement.purchases.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PurchaseDetailResponse(UUID id, String purchaseNumber,
        UUID supplierId, String supplierName, String supplierRif,
        UUID createdBy, String createdByName, String status, String notes,
        BigDecimal subtotal, LocalDateTime confirmedAt,
        LocalDateTime cancelledAt, String cancelledReason,
        LocalDateTime createdAt, LocalDateTime updatedAt,
        List<LineDetailResponse> lines) {
    public record LineDetailResponse(UUID id, UUID productId,
            String productName, String productCode, BigDecimal quantity,
            BigDecimal unitCost, BigDecimal subtotal,
            List<AllocationDetail> allocations,
            List<StockItemDetail> stockItems) {
        public record AllocationDetail(UUID warehouseId, String warehouseCode,
                String warehouseName, BigDecimal allocatedQuantity) {
        }

        public record StockItemDetail(UUID warehouseId, String warehouseCode,
                String warehouseName, int quantity, int reserved,
                int available) {
        }
    }
}
