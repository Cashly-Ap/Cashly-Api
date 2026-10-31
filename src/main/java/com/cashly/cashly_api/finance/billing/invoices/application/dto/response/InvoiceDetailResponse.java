package com.cashly.cashly_api.finance.billing.invoices.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record InvoiceDetailResponse(UUID id, String invoiceNumber,
        UUID customerId, String customerName, String customerRif,
        UUID createdBy, String createdByName, UUID orderId, String orderNumber,
        String status, String statusLabel, String paymentType, String notes,
        BigDecimal subtotal, BigDecimal creditApplied, BigDecimal debitApplied,
        BigDecimal total, List<NoteInfo> notesList, LocalDateTime cancelledAt,
        String cancelledReason, LocalDateTime createdAt,
        LocalDateTime updatedAt, LocalDate issueDate, LocalDate dueDate,
        List<InvoiceLineDetailResponse> lines, BigDecimal returnedTotal,
        String returnStatus, List<ReturnDetail> returns) {
    public InvoiceDetailResponse(UUID id, String invoiceNumber, UUID customerId,
            String customerName, String customerRif, UUID createdBy,
            String createdByName, UUID orderId, String orderNumber,
            String status, String statusLabel, String paymentType, String notes,
            BigDecimal subtotal, BigDecimal creditApplied,
            BigDecimal debitApplied, BigDecimal total, List<NoteInfo> notesList,
            LocalDateTime cancelledAt, String cancelledReason,
            LocalDateTime createdAt, LocalDateTime updatedAt,
            List<InvoiceLineDetailResponse> lines, BigDecimal returnedTotal,
            String returnStatus, List<ReturnDetail> returns) {
        this(id, invoiceNumber, customerId, customerName, customerRif,
                createdBy, createdByName, orderId, orderNumber, status,
                statusLabel, paymentType, notes, subtotal, creditApplied,
                debitApplied, total, notesList, cancelledAt, cancelledReason,
                createdAt, updatedAt,
                createdAt != null ? createdAt.toLocalDate() : null,
                createdAt != null ? createdAt.toLocalDate() : null, lines,
                returnedTotal, returnStatus, returns);
    }

    public record NoteInfo(UUID id, String noteNumber, String type,
            BigDecimal amount, String reason, LocalDateTime applicationDate) {
    }

    public record InvoiceLineDetailResponse(UUID id, UUID productId,
            String productName, String productCode, BigDecimal quantity,
            BigDecimal unitPrice, BigDecimal lineTotal,
            List<WarehouseAllocationResponse> warehouseAllocations,
            List<WarehouseStockDetail> warehouseStocks,
            BigDecimal returnedQuantity) {
    }

    public record WarehouseAllocationResponse(UUID id, UUID warehouseId,
            String warehouseCode, String warehouseName, Integer available,
            BigDecimal allocated) {
    }

    public record WarehouseStockDetail(UUID warehouseId, String warehouseCode,
            String warehouseName, int quantity, int reserved, int available) {
    }

    public record ReturnDetail(UUID id, String returnNumber, String returnType,
            String reason, BigDecimal total, LocalDateTime createdAt,
            String createdByName, List<ReturnLineDetail> lines) {
    }

    public record ReturnLineDetail(UUID productId, String productName,
            String productCode, BigDecimal quantity, BigDecimal unitPrice,
            BigDecimal subtotal, List<ReturnLineAllocationDetail> allocations) {
    }

    public record ReturnLineAllocationDetail(UUID warehouseId,
            String warehouseName, BigDecimal allocated) {
    }
}
