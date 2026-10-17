package com.cashly.cashly_api.finance.treasury.receivables.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CustomerDebtSummaryResponse(
        UUID customerId,
        String customerName,
        String customerRif,
        String customerPhone,
        String customerEmail,
        BigDecimal totalDebt,
        BigDecimal pendingPaymentsAmount,
        BigDecimal availableDebt,
        BigDecimal creditLimit,
        BigDecimal availableCredit,
        BigDecimal customerBalance,
        List<CustomerInvoiceDebtResponse> invoicesWithDebt,
        long pendingPaymentsCount
) {}
