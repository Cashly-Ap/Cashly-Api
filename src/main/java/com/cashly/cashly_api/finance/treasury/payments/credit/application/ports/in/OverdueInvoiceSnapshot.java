package com.cashly.cashly_api.finance.treasury.payments.credit.application.ports.in;
import java.math.BigDecimal;
import java.time.LocalDate;
public record OverdueInvoiceSnapshot(String invoiceNumber, BigDecimal amount,
        LocalDate dueDate, int daysOverdue) {}
