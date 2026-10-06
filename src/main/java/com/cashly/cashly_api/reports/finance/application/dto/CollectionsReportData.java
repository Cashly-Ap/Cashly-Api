package com.cashly.cashly_api.reports.finance.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CollectionsReportData(
        LocalDate reportDate,
        UUID customerId,
        LocalDate agingFrom,
        List<CollectionRow> rows,
        Summary summary
) {
    public record CollectionRow(
            String customerName,
            String rif,
            String invoiceNumber,
            LocalDate dueDate,
            int daysOverdue,
            BigDecimal pendingBalance
    ) {}

    public record Summary(
            int totalInvoices,
            BigDecimal totalOverdue,
            BigDecimal age0_30,
            BigDecimal age31_60,
            BigDecimal age61_90,
            BigDecimal age91_120,
            BigDecimal age120Plus
    ) {}
}
