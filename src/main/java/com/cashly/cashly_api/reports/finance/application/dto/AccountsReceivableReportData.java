package com.cashly.cashly_api.reports.finance.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record AccountsReceivableReportData(
        LocalDate reportDate,
        boolean overdueOnly,
        List<AccountReceivableRow> rows,
        Summary summary
) {
    public record AccountReceivableRow(
            UUID customerId,
            String customerName,
            String rif,
            BigDecimal creditLimit,
            BigDecimal pendingBalance,
            BigDecimal available,
            BigDecimal age0_30,
            BigDecimal age31_60,
            BigDecimal age61_90,
            BigDecimal age91_120,
            BigDecimal age120Plus
    ) {}

    public record Summary(
            BigDecimal totalReceivable,
            BigDecimal notYetDue,
            BigDecimal overdue,
            int customersExceededCredit
    ) {}
}
