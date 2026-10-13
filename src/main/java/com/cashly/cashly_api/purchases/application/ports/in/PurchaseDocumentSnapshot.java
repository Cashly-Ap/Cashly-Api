package com.cashly.cashly_api.purchases.application.ports.in;

import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PurchaseDocumentSnapshot(
        UUID id, String purchaseNumber, String supplierName, String supplierRif, String status,
        String notes, String cancelledReason, LocalDateTime createdAt, BigDecimal subtotal, List<Line> lines) {
    public PurchaseDocumentSnapshot { lines = List.copyOf(lines); }
    public record Line(String productCode, String productName, BigDecimal quantity, BigDecimal unitCost, BigDecimal subtotal) {}
}
