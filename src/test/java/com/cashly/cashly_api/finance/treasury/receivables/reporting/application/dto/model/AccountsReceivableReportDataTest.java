package com.cashly.cashly_api.finance.treasury.receivables.reporting.application.dto.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AccountsReceivableReportData Unit Tests")
class AccountsReceivableReportDataTest {

    @Test
    @DisplayName("should create report data with all fields")
    void should_create_report_data() {
        var row = new AccountsReceivableReportData.AccountReceivableRow(
                UUID.randomUUID(), "Customer A", "J-12345678",
                BigDecimal.valueOf(10000), BigDecimal.valueOf(5000), BigDecimal.valueOf(5000),
                BigDecimal.valueOf(1000), BigDecimal.valueOf(2000), BigDecimal.valueOf(1000),
                BigDecimal.valueOf(500), BigDecimal.valueOf(500));

        var summary = new AccountsReceivableReportData.Summary(
                BigDecimal.valueOf(5000), BigDecimal.valueOf(3000), BigDecimal.valueOf(2000), 1);

        var data = new AccountsReceivableReportData(
                LocalDate.of(2026, 6, 15), true, List.of(row), summary);

        assertEquals(LocalDate.of(2026, 6, 15), data.reportDate());
        assertTrue(data.overdueOnly());
        assertEquals(1, data.rows().size());
        assertEquals("Customer A", data.rows().get(0).customerName());
        assertEquals(BigDecimal.valueOf(5000), data.summary().totalReceivable());
        assertEquals(1, data.summary().customersExceededCredit());
    }

    @Test
    @DisplayName("should create report data with empty rows")
    void should_create_report_data_with_empty_rows() {
        var summary = new AccountsReceivableReportData.Summary(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0);

        var data = new AccountsReceivableReportData(
                LocalDate.now(), false, List.of(), summary);

        assertFalse(data.overdueOnly());
        assertTrue(data.rows().isEmpty());
        assertEquals(BigDecimal.ZERO, data.summary().totalReceivable());
    }
}
