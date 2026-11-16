package com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.excel.services;

import com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.dto.model.CashRegisterExportRow;
import com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.dto.model.CustomerSummary;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.dto.response.DailyTotals;
import com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.dto.model.ReportType;
import com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.ports.out.CashRegisterExcelPort;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.entity.CashRegisterDetail;
import com.cashly.cashly_api.finance.treasury.cash_register.application.ports.out.CashRegisterQueryGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GenerateCashRegisterExcelUseCaseImpl Unit Tests")
class GenerateCashRegisterExcelUseCaseImplTest {

    @Mock
    private CashRegisterQueryGateway repository;

    @Mock
    private CashRegisterExcelPort excelPort;

    private GenerateCashRegisterExcelUseCaseImpl useCase;

    private static final LocalDate DATE_FROM = LocalDate.of(2025, 6, 15);
    private static final LocalDate DATE_TO = LocalDate.of(2025, 6, 15);

    @BeforeEach
    void setUp() {
        useCase = new GenerateCashRegisterExcelUseCaseImpl(repository,
                excelPort);
    }

    @Test
    @DisplayName("should generate DETAILED Excel by fetching rows and delegating to port")
    void shouldGenerateDetalladoExcelByFetchingRowsAndDelegatingToPort() {
        List<CashRegisterExportRow> mockRows = List.of(CashRegisterExportRow.of(
                DATE_FROM, "FAC", "INV-001", BigDecimal.valueOf(1000),
                BigDecimal.valueOf(500), BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, "Cliente A", "VENTAS", 1));
        byte[] expectedBytes = new byte[]{1, 2, 3};

        when(repository.findExportRows(DATE_FROM, DATE_TO))
                .thenReturn(mockRows);
        when(excelPort.generateExcel(mockRows)).thenReturn(expectedBytes);

        byte[] result = useCase.execute(DATE_FROM, DATE_TO,
                ReportType.DETAILED);

        assertNotNull(result);
        assertArrayEquals(expectedBytes, result);
        verify(repository).findExportRows(DATE_FROM, DATE_TO);
        verify(excelPort).generateExcel(mockRows);
    }

    @Test
    @DisplayName("should generate DETAILED Excel with empty rows when no data")
    void shouldGenerateDetalladoExcelWithEmptyRowsWhenNoData() {
        when(repository.findExportRows(DATE_FROM, DATE_TO))
                .thenReturn(List.of());
        when(excelPort.generateExcel(List.of())).thenReturn(new byte[0]);

        byte[] result = useCase.execute(DATE_FROM, DATE_TO,
                ReportType.DETAILED);

        assertNotNull(result);
        assertEquals(0, result.length);
        verify(repository).findExportRows(DATE_FROM, DATE_TO);
        verify(excelPort).generateExcel(List.of());
    }

    @Test
    @DisplayName("should propagate exception when repository fails for DETAILED")
    void shouldPropagateExceptionWhenRepositoryFailsForDetallado() {
        when(repository.findExportRows(DATE_FROM, DATE_TO))
                .thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class,
                () -> useCase.execute(DATE_FROM, DATE_TO, ReportType.DETAILED));
        verify(repository).findExportRows(DATE_FROM, DATE_TO);
        verifyNoInteractions(excelPort);
    }

    @Test
    @DisplayName("should generate SIMPLE report with payment summary and totals")
    void shouldGenerateSimpleReport() {
        var detail = CashRegisterDetail.create(UUID.randomUUID(), "Efectivo", 5,
                BigDecimal.valueOf(2500));
        List<CashRegisterDetail> paymentSummary = List.of(detail);

        DailyTotals totals = new DailyTotals(BigDecimal.valueOf(2500), 5,
                BigDecimal.ZERO, 0, BigDecimal.valueOf(500), 1, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.valueOf(3000), 3,
                BigDecimal.valueOf(3000), BigDecimal.valueOf(2500),
                BigDecimal.valueOf(500), paymentSummary);

        byte[] expectedBytes = new byte[]{4, 5, 6};

        when(repository.findPaymentMethodSummary(DATE_FROM, DATE_TO))
                .thenReturn(paymentSummary);
        when(repository.findRangeTotals(DATE_FROM, DATE_TO)).thenReturn(totals);
        when(excelPort.generateSimpleExcel(paymentSummary, totals))
                .thenReturn(expectedBytes);

        byte[] result = useCase.execute(DATE_FROM, DATE_TO, ReportType.SIMPLE);

        assertNotNull(result);
        assertArrayEquals(expectedBytes, result);
        verify(repository).findPaymentMethodSummary(DATE_FROM, DATE_TO);
        verify(repository).findRangeTotals(DATE_FROM, DATE_TO);
        verify(excelPort).generateSimpleExcel(paymentSummary, totals);
    }

    @Test
    @DisplayName("should generate GROUPED report with customer summaries")
    void shouldGenerateAgrupadoReport() {
        List<CustomerSummary> summaries = List.of(new CustomerSummary(
                "Cliente A", BigDecimal.valueOf(5000), BigDecimal.valueOf(4800),
                BigDecimal.valueOf(200), BigDecimal.valueOf(4600)));
        byte[] expectedBytes = new byte[]{7, 8, 9};

        when(repository.findCustomerSummary(DATE_FROM, DATE_TO))
                .thenReturn(summaries);
        when(excelPort.generateGroupedExcel(summaries))
                .thenReturn(expectedBytes);

        byte[] result = useCase.execute(DATE_FROM, DATE_TO, ReportType.GROUPED);

        assertNotNull(result);
        assertArrayEquals(expectedBytes, result);
        verify(repository).findCustomerSummary(DATE_FROM, DATE_TO);
        verify(excelPort).generateGroupedExcel(summaries);
    }
}
