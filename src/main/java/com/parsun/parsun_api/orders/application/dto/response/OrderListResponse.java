package com.parsun.parsun_api.orders.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderListResponse(
        UUID id,
        String orderNumber,
        UUID customerId,
        String customerName,
        String status,
        BigDecimal subtotal,
        int lineCount,
        UUID invoiceId,
        LocalDateTime createdAt
) {}
