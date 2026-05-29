package com.parsun.parsun_api.receivables.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CustomerInvoiceDebtResponse(
        UUID invoiceId,
        String invoiceNumber,
        String status,
        LocalDate invoiceDate,
        BigDecimal subtotal,
        BigDecimal returnedTotal,
        BigDecimal totalPaid,
        BigDecimal balance,
        UUID pendingPaymentId,
        String pendingPaymentStatus
) {}
