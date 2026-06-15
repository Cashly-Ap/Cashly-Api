package com.parsun.parsun_api.customers.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OverdueInvoiceResponse(
        String invoiceNumber,
        BigDecimal amount,
        LocalDate dueDate,
        int daysOverdue
) {}
