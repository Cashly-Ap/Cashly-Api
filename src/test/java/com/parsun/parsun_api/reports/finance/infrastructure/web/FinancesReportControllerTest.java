package com.parsun.parsun_api.reports.finance.infrastructure.web;

import com.parsun.parsun_api.reports.finance.application.usecases.GenerateAccountsReceivableReportPdfUseCase;
import com.parsun.parsun_api.reports.finance.application.usecases.GenerateCollectionsReportPdfUseCase;
import com.parsun.parsun_api.reports.finance.application.usecases.GeneratePaymentHistoryReportPdfUseCase;
import com.parsun.parsun_api.reports.finance.application.usecases.GeneratePaymentPdfUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FinancesReportController Unit Tests")
class FinancesReportControllerTest {

    @Mock
    private GeneratePaymentPdfUseCase generatePaymentPdfUseCase;

    @Mock
    private GenerateAccountsReceivableReportPdfUseCase generateAccountsReceivableReportPdfUseCase;

    @Mock
    private GeneratePaymentHistoryReportPdfUseCase generatePaymentHistoryReportPdfUseCase;

    @Mock
    private GenerateCollectionsReportPdfUseCase generateCollectionsReportPdfUseCase;

    @InjectMocks
    private FinancesReportController controller;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("should return payment PDF as attachment")
    void shouldGetPaymentPdf() {
        UUID paymentId = UUID.randomUUID();
        byte[] pdfContent = "PDF content".getBytes();

        when(generatePaymentPdfUseCase.execute(paymentId)).thenReturn(pdfContent);

        ResponseEntity<byte[]> result = controller.getPaymentPdf(paymentId);

        assertEquals(MediaType.APPLICATION_PDF, result.getHeaders().getContentType());
        assertTrue(result.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION).get(0)
                .contains("attachment; filename=\"pago-" + paymentId + ".pdf\""));
        assertArrayEquals(pdfContent, result.getBody());
        assertEquals(200, result.getStatusCodeValue());
        verify(generatePaymentPdfUseCase).execute(paymentId);
    }

    @Test
    @DisplayName("should return accounts receivable PDF as attachment")
    void shouldGetAccountsReceivablePdf() {
        UUID customerId = UUID.randomUUID();
        byte[] pdfContent = "AR PDF".getBytes();

        when(generateAccountsReceivableReportPdfUseCase.execute(customerId, false))
                .thenReturn(pdfContent);

        ResponseEntity<byte[]> result = controller.getAccountsReceivablePdf(customerId, false);

        assertEquals(MediaType.APPLICATION_PDF, result.getHeaders().getContentType());
        assertTrue(result.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION).get(0)
                .contains("cuentas-por-cobrar-"));
        assertArrayEquals(pdfContent, result.getBody());
        verify(generateAccountsReceivableReportPdfUseCase).execute(customerId, false);
    }

    @Test
    @DisplayName("should return accounts receivable PDF with overdue filter")
    void shouldGetAccountsReceivablePdfWithOverdueFilter() {
        byte[] pdfContent = "Overdue AR PDF".getBytes();

        when(generateAccountsReceivableReportPdfUseCase.execute(null, true))
                .thenReturn(pdfContent);

        ResponseEntity<byte[]> result = controller.getAccountsReceivablePdf(null, true);

        assertArrayEquals(pdfContent, result.getBody());
        verify(generateAccountsReceivableReportPdfUseCase).execute(null, true);
    }

    @Test
    @DisplayName("should return payment history PDF as attachment")
    void shouldGetPaymentHistoryPdf() {
        LocalDate dateFrom = LocalDate.of(2026, 1, 1);
        LocalDate dateTo = LocalDate.of(2026, 12, 31);
        UUID customerId = UUID.randomUUID();
        byte[] pdfContent = "History PDF".getBytes();

        when(generatePaymentHistoryReportPdfUseCase.execute(dateFrom, dateTo, customerId, "APPLIED", "CASH"))
                .thenReturn(pdfContent);

        ResponseEntity<byte[]> result = controller.getPaymentHistoryPdf(
                dateFrom, dateTo, customerId, "APPLIED", "CASH"
        );

        assertEquals(MediaType.APPLICATION_PDF, result.getHeaders().getContentType());
        assertTrue(result.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION).get(0)
                .contains("historial-pagos-"));
        assertArrayEquals(pdfContent, result.getBody());
        verify(generatePaymentHistoryReportPdfUseCase).execute(dateFrom, dateTo, customerId, "APPLIED", "CASH");
    }

    @Test
    @DisplayName("should return collections PDF as attachment")
    void shouldGetCollectionsPdf() {
        UUID customerId = UUID.randomUUID();
        LocalDate agingFrom = LocalDate.now().minusMonths(3);
        byte[] pdfContent = "Collections PDF".getBytes();

        when(generateCollectionsReportPdfUseCase.execute(customerId, agingFrom))
                .thenReturn(pdfContent);

        ResponseEntity<byte[]> result = controller.getCollectionsPdf(customerId, agingFrom);

        assertEquals(MediaType.APPLICATION_PDF, result.getHeaders().getContentType());
        assertTrue(result.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION).get(0)
                .contains("cobranzas-"));
        assertArrayEquals(pdfContent, result.getBody());
        verify(generateCollectionsReportPdfUseCase).execute(customerId, agingFrom);
    }

    @Test
    @DisplayName("should return collections PDF without filters")
    void shouldGetCollectionsPdfWithoutFilters() {
        byte[] pdfContent = "All collections PDF".getBytes();

        when(generateCollectionsReportPdfUseCase.execute(null, null))
                .thenReturn(pdfContent);

        ResponseEntity<byte[]> result = controller.getCollectionsPdf(null, null);

        assertArrayEquals(pdfContent, result.getBody());
        verify(generateCollectionsReportPdfUseCase).execute(null, null);
    }
}
