package com.cashly.cashly_api.receivables.reporting.infrastructure.web;

import com.cashly.cashly_api.receivables.reporting.application.generate_accounts_receivable_report_pdf.use_case.GenerateAccountsReceivableReportPdfUseCase;
import com.cashly.cashly_api.receivables.reporting.application.generate_collections_report_pdf.use_case.GenerateCollectionsReportPdfUseCase;
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
class ReceivablesReportsControllerTest {

    @Mock
    private GenerateAccountsReceivableReportPdfUseCase generateAccountsReceivableReportPdfUseCase;

    @Mock
    private GenerateCollectionsReportPdfUseCase generateCollectionsReportPdfUseCase;

    @InjectMocks
    private ReceivablesReportsController controller;

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
