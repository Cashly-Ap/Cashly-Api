package com.parsun.parsun_api.receivables.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreditStatusResponse(
        UUID customerId,
        String customerName,
        String customerRif,
        BigDecimal creditLimit,
        BigDecimal pendingBalance,
        BigDecimal unconsumedAdvances,
        BigDecimal availableCredit,
        List<OverdueInvoiceResponse> overdueInvoices,
        List<AdvanceResponse> advances,
        boolean hasOverdue,
        boolean canCreateCreditInvoice
) {}
