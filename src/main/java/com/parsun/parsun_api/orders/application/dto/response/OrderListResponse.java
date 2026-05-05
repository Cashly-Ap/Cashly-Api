package com.parsun.parsun_api.orders.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderListResponse(
        UUID id,
        String orderNumber,
        UUID customerId,
        String customerName,
        String status,
        LocalDate deliveryDate,
        BigDecimal subtotal,
        int lineCount,
        UUID invoiceId,
        LocalDateTime createdAt
) {}
