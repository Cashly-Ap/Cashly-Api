package com.cashly.cashly_api.invoices.application.api;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
public record InvoiceCreditLedgerEntry(UUID id, String number, boolean pendingPayment,
        BigDecimal subtotal, BigDecimal returnedTotal, LocalDate dueDate) {}
