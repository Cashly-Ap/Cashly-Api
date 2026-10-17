package com.cashly.cashly_api.finance.treasury.receivables.reporting.application.dto.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CollectionsReportData Unit Tests")
class CollectionsReportDataTest {

    @Test
    @DisplayName("should create collections report data with all fields")
    void should_create_collections_data() {
        var row = new CollectionsReportData.CollectionRow(
                "Customer A", "J-12345678", "FAC-001",
                LocalDate.of(2026, 5, 1), 45, BigDecimal.valueOf(2500));

        var summary = new CollectionsReportData.Summary(
                10, BigDecimal.valueOf(25000),
                BigDecimal.valueOf(5000), BigDecimal.valueOf(5000), BigDecimal.valueOf(5000),
                BigDecimal.valueOf(5000), BigDecimal.valueOf(5000));

        var data = new CollectionsReportData(
                LocalDate.of(2026, 6, 15), UUID.randomUUID(),
                LocalDate.of(2026, 3, 1), List.of(row), summary);

        assertEquals(LocalDate.of(2026, 6, 15), data.reportDate());
        assertEquals(1, data.rows().size());
        assertEquals("FAC-001", data.rows().get(0).invoiceNumber());
        assertEquals(10, data.summary().totalInvoices());
    }

    @Test
    @DisplayName("should create collections data with null customer")
    void should_create_collections_data_with_null_customer() {
        var summary = new CollectionsReportData.Summary(
                0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

        var data = new CollectionsReportData(
                LocalDate.now(), null, null, List.of(), summary);

        assertNull(data.customerId());
        assertNull(data.agingFrom());
    }
}
