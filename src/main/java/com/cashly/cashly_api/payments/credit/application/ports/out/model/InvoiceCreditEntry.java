package com.cashly.cashly_api.payments.credit.application.ports.out.model;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
public record InvoiceCreditEntry(UUID id, String number, boolean pendingPayment,
        BigDecimal subtotal, BigDecimal returnedTotal, LocalDate dueDate) {}
