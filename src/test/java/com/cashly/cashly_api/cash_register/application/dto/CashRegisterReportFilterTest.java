package com.cashly.cashly_api.cash_register.application.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;

class CashRegisterReportFilterTest {

    @Test
    void should_reject_inverted_date_range() {
        assertThrows(IllegalArgumentException.class, () -> new CashRegisterReportFilter(
                LocalDate.of(2026, 8, 31), LocalDate.of(2026, 8, 1),
                null, null, null));
    }
}
