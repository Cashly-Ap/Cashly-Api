package com.parsun.parsun_api.invoices.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record InvoiceDetailResponse(
        UUID id,
        String invoiceNumber,
        UUID customerId,
        String customerName,
        String customerRif,
        UUID createdBy,
        String createdByName,
        UUID orderId,
        String orderNumber,
        String status,
        String paymentType,
        String notes,
        BigDecimal subtotal,
        LocalDateTime cancelledAt,
        String cancelledReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<InvoiceLineDetailResponse> lines
) {
    public record InvoiceLineDetailResponse(
            UUID id,
            UUID productId,
            String productName,
            String productCode,
            BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal,
            List<WarehouseAllocationResponse> warehouseAllocations,
            List<WarehouseStockDetail> warehouseStocks
    ) {}

    public record WarehouseAllocationResponse(
            UUID id,
            UUID warehouseId,
            String warehouseCode,
            String warehouseName,
            Integer available,
            BigDecimal allocated
    ) {}

    public record WarehouseStockDetail(
            UUID warehouseId,
            String warehouseCode,
            String warehouseName,
            int quantity,
            int reserved,
            int available
    ) {}
}
