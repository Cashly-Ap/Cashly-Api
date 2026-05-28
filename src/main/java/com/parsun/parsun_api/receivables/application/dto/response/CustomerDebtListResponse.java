package com.parsun.parsun_api.receivables.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CustomerDebtListResponse(
        UUID customerId,
        String customerName,
        String customerRif,
        BigDecimal totalDebt,
        BigDecimal pendingPaymentsAmount,
        long pendingInvoicesCount,
        LocalDate oldestInvoiceDate
) {}
