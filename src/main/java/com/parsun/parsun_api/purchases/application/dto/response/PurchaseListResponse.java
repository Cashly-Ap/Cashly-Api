package com.parsun.parsun_api.purchases.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PurchaseListResponse(
        UUID id,
        String purchaseNumber,
        UUID supplierId,
        String supplierName,
        String status,
        BigDecimal subtotal,
        int lineCount,
        LocalDateTime createdAt
) {}
