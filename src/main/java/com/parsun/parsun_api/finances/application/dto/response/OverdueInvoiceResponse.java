package com.parsun.parsun_api.finances.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OverdueInvoiceResponse(
        String invoiceNumber,
        BigDecimal amount,
        LocalDate dueDate,
        int daysOverdue
) {}
