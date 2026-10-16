package com.cashly.cashly_api.payments.reporting.application.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PaymentHistoryReportData Unit Tests")
class PaymentHistoryReportDataTest {

    @Test
    @DisplayName("should create payment history data with all fields")
    void should_create_payment_history_data() {
        var row = new PaymentHistoryReportData.PaymentHistoryRow(
                "PAG-001", "Customer A", "APPLIED", "CASH",
                BigDecimal.valueOf(1000), LocalDateTime.of(2026, 6, 15, 10, 30), "COMPLETED");

        var summary = new PaymentHistoryReportData.Summary(
                5, BigDecimal.valueOf(5000), 3, 2);

        var data = new PaymentHistoryReportData(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31),
                UUID.randomUUID(), "APPLIED", "CASH",
                List.of(row), summary);

        assertEquals(LocalDate.of(2026, 1, 1), data.dateFrom());
        assertEquals(LocalDate.of(2026, 12, 31), data.dateTo());
        assertEquals(1, data.rows().size());
        assertEquals("PAG-001", data.rows().get(0).paymentNumber());
        assertEquals(5, data.summary().totalPayments());
    }

    @Test
    @DisplayName("should create payment history data with null filters")
    void should_create_payment_history_data_with_null_filters() {
        var summary = new PaymentHistoryReportData.Summary(0, BigDecimal.ZERO, 0, 0);

        var data = new PaymentHistoryReportData(
                null, null, null, null, null, List.of(), summary);

        assertNull(data.dateFrom());
        assertNull(data.customerId());
        assertNull(data.type());
        assertNull(data.method());
    }
}
