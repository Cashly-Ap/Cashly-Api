package com.cashly.cashly_api.finance.treasury.payments.credit.application.ports.out.model;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
public record InvoiceCreditEntry(UUID id, String number, boolean pendingPayment,
        BigDecimal subtotal, BigDecimal returnedTotal, LocalDate dueDate) {
}
