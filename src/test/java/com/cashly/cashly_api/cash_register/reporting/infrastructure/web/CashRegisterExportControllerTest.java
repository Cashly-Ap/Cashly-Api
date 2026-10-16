package com.cashly.cashly_api.cash_register.reporting.infrastructure.web;

import com.cashly.cashly_api.cash_register.reporting.application.dto.ReportType;
import com.cashly.cashly_api.cash_register.reporting.application.dto.ReportFormat;
import com.cashly.cashly_api.cash_register.reporting.application.dto.CashRegisterReportFilter;
import com.cashly.cashly_api.cash_register.reporting.application.excel.GenerateCashRegisterExcelUseCase;
import com.cashly.cashly_api.cash_register.reporting.application.range.GenerateCashRegisterRangeReportUseCase;
import com.cashly.cashly_api.cash_register.reporting.application.session.GenerateCashRegisterReportUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CashRegisterExportController Unit Tests")
class CashRegisterExportControllerTest {

    @Mock
    private GenerateCashRegisterExcelUseCase generateCashRegisterExcelUseCase;

    @Mock
    private GenerateCashRegisterReportUseCase generateCashRegisterReportUseCase;

    @Mock
    private GenerateCashRegisterRangeReportUseCase generateCashRegisterRangeReportUseCase;

    @InjectMocks
    private CashRegisterExportController controller;

    @Test
    @DisplayName("should return Excel file with correct headers for valid date range")
    void shouldReturnExcelFileWithCorrectHeadersForValidDateRange() {
        LocalDate dateFrom = LocalDate.of(2025, 6, 15);
        LocalDate dateTo = LocalDate.of(2025, 6, 15);
        byte[] expectedBytes = new byte[]{1, 2, 3};

        when(generateCashRegisterExcelUseCase.execute(dateFrom, dateTo, ReportType.DETAILED)).thenReturn(expectedBytes);

        ResponseEntity<byte[]> response = controller.exportExcel(dateFrom, dateTo, ReportType.DETAILED);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(
                MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
                response.getHeaders().getContentType()
        );
        String contentDisposition = response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
        assertNotNull(contentDisposition);
        assertTrue(contentDisposition.contains("cierre-caja-detallado-2025-06-15_2025-06-15.xlsx"));
        assertTrue(contentDisposition.startsWith("attachment"));
        assertArrayEquals(expectedBytes, response.getBody());
        verify(generateCashRegisterExcelUseCase).execute(dateFrom, dateTo, ReportType.DETAILED);
    }

    @Test
    @DisplayName("should return Excel file with date-range-specific filename")
    void shouldReturnExcelFileWithDateRangeSpecificFilename() {
        LocalDate dateFrom = LocalDate.of(2025, 12, 1);
        LocalDate dateTo = LocalDate.of(2025, 12, 31);
        byte[] expectedBytes = new byte[]{4, 5, 6};

        when(generateCashRegisterExcelUseCase.execute(dateFrom, dateTo, ReportType.DETAILED)).thenReturn(expectedBytes);

        ResponseEntity<byte[]> response = controller.exportExcel(dateFrom, dateTo, ReportType.DETAILED);

        String contentDisposition = response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
        assertTrue(contentDisposition.contains("cierre-caja-detallado-2025-12-01_2025-12-31.xlsx"));
        assertArrayEquals(expectedBytes, response.getBody());
        verify(generateCashRegisterExcelUseCase).execute(dateFrom, dateTo, ReportType.DETAILED);
    }

    @Test
    @DisplayName("should return empty Excel when no data")
    void shouldReturnEmptyExcelWhenNoData() {
        LocalDate dateFrom = LocalDate.of(2025, 6, 15);
        LocalDate dateTo = LocalDate.of(2025, 6, 15);

        when(generateCashRegisterExcelUseCase.execute(dateFrom, dateTo, ReportType.DETAILED)).thenReturn(new byte[0]);

        ResponseEntity<byte[]> response = controller.exportExcel(dateFrom, dateTo, ReportType.DETAILED);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
        assertEquals(0, response.getBody().length);
        verify(generateCashRegisterExcelUseCase).execute(dateFrom, dateTo, ReportType.DETAILED);
    }

    @Test
    void shouldReturnDetailedPdfForSelectedSession() {
        UUID id = UUID.randomUUID();
        byte[] expected = new byte[]{9, 8, 7};
        when(generateCashRegisterReportUseCase.execute(id, ReportFormat.PDF, ReportType.DETAILED))
                .thenReturn(expected);

        ResponseEntity<byte[]> response = controller.exportSession(
                id, ReportFormat.PDF, ReportType.DETAILED);

        assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());
        assertTrue(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)
                .contains("caja-" + id + "-detailed.pdf"));
        assertArrayEquals(expected, response.getBody());
    }

    @Test
    void shouldReturnFilteredGroupedPdfForDateRange() {
        LocalDate dateFrom = LocalDate.of(2026, 8, 1);
        LocalDate dateTo = LocalDate.of(2026, 8, 31);
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        byte[] expected = new byte[]{7, 8, 9};
        var filter = new CashRegisterReportFilter(
                dateFrom, dateTo, customerId, productId, categoryId);
        when(generateCashRegisterRangeReportUseCase.execute(filter, ReportFormat.PDF, ReportType.GROUPED))
                .thenReturn(expected);

        ResponseEntity<byte[]> response = controller.exportRangeReport(
                dateFrom, dateTo, ReportFormat.PDF, ReportType.GROUPED,
                customerId, productId, categoryId);

        assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());
        assertTrue(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)
                .contains("caja-2026-08-01_2026-08-31-grouped.pdf"));
        assertArrayEquals(expected, response.getBody());
    }
}
