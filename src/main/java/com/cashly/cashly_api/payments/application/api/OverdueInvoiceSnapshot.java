package com.cashly.cashly_api.payments.application.api;
import java.math.BigDecimal;
import java.time.LocalDate;
public record OverdueInvoiceSnapshot(String invoiceNumber, BigDecimal amount,
        LocalDate dueDate, int daysOverdue) {}
