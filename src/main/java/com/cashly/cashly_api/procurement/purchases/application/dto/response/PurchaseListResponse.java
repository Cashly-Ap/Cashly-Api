package com.cashly.cashly_api.procurement.purchases.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PurchaseListResponse(UUID id, String purchaseNumber,
        UUID supplierId, String supplierName, String status,
        String paymentType, LocalDate issueDate, LocalDate dueDate,
        BigDecimal subtotal, int lineCount, LocalDateTime createdAt) {

    public PurchaseListResponse(UUID id, String purchaseNumber,
            UUID supplierId, String supplierName, String status,
            BigDecimal subtotal, int lineCount, LocalDateTime createdAt) {
        this(id, purchaseNumber, supplierId, supplierName, status, null, null,
                null, subtotal, lineCount, createdAt);
    }
}
