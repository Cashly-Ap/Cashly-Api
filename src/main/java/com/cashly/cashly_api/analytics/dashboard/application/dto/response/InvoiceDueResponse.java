package com.cashly.cashly_api.analytics.dashboard.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record InvoiceDueResponse(UUID invoiceId, String invoiceNumber,
        UUID customerId, String customerName, LocalDate dueDate,
        BigDecimal amount) {
}
