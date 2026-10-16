package com.cashly.cashly_api.payments.reporting.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PaymentHistoryReportData(
        LocalDate dateFrom,
        LocalDate dateTo,
        UUID customerId,
        String type,
        String method,
        List<PaymentHistoryRow> rows,
        Summary summary
) {
    public record PaymentHistoryRow(
            String paymentNumber,
            String customerName,
            String type,
            String method,
            BigDecimal amount,
            LocalDateTime date,
            String status
    ) {}

    public record Summary(
            int totalPayments,
            BigDecimal totalAmount,
            int paymentCount,
            int advanceCount
    ) {}
}
