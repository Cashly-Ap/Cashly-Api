package com.cashly.cashly_api.procurement.purchases.application.ports.in;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PurchaseDocumentRow(UUID id, String purchaseNumber,
        String supplierName, String status, BigDecimal subtotal,
        LocalDateTime createdAt) {
}
