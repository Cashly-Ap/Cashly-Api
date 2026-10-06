package com.cashly.cashly_api.reports.invoices.infrastructure.web;

import com.cashly.cashly_api.invoices.application.dto.response.InvoiceListResponse;
import com.cashly.cashly_api.invoices.application.usecases.GetInvoicesUseCase;
import com.cashly.cashly_api.reports.invoices.application.usecases.GenerateInvoicePdfUseCase;
import com.cashly.cashly_api.reports.invoices.application.usecases.GenerateInvoicesReportPdfUseCase;
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
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InvoicesReportController Unit Tests")
class InvoicesReportControllerTest {

    @Mock
    private GenerateInvoicePdfUseCase generateInvoicePdfUseCase;

    @Mock
    private GenerateInvoicesReportPdfUseCase generateInvoicesReportPdfUseCase;

    @Mock
    private GetInvoicesUseCase getInvoicesUseCase;

    @InjectMocks
    private InvoicesReportController controller;

    @Test
    @DisplayName("should return invoice PDF as attachment")
    void should_get_invoice_pdf() {
        UUID invoiceId = UUID.randomUUID();
        byte[] pdfContent = "Invoice PDF".getBytes();

        when(generateInvoicePdfUseCase.execute(invoiceId)).thenReturn(pdfContent);

        ResponseEntity<byte[]> result = controller.getInvoicePdf(invoiceId);

        assertEquals(MediaType.APPLICATION_PDF, result.getHeaders().getContentType());
        assertTrue(result.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION).get(0)
                .contains("nota-" + invoiceId + ".pdf"));
        assertArrayEquals(pdfContent, result.getBody());
        assertEquals(200, result.getStatusCodeValue());
        verify(generateInvoicePdfUseCase).execute(invoiceId);
    }

    @Test
    @DisplayName("should return invoices report PDF as attachment")
    void should_get_invoices_report_pdf() {
        UUID customerId = UUID.randomUUID();
        LocalDate dateFrom = LocalDate.of(2026, 1, 1);
        LocalDate dateTo = LocalDate.of(2026, 12, 31);
        List<String> status = List.of("PAID");
        UUID productId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        byte[] pdfContent = "Invoices report PDF".getBytes();
        List<InvoiceListResponse> invoices = List.of();

        when(getInvoicesUseCase.execute(dateFrom, dateTo, customerId, status, null, productId, categoryId,
                "createdAt", "desc")).thenReturn(invoices);
        when(generateInvoicesReportPdfUseCase.execute(invoices, "SIMPLE")).thenReturn(pdfContent);

        ResponseEntity<byte[]> result = controller.getInvoicesReportPdf(
                dateFrom, dateTo, customerId, status, productId, categoryId, "SIMPLE");

        assertEquals(MediaType.APPLICATION_PDF, result.getHeaders().getContentType());
        assertTrue(result.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION).get(0)
                .contains("reporte-facturas-"));
        assertArrayEquals(pdfContent, result.getBody());
        verify(getInvoicesUseCase).execute(dateFrom, dateTo, customerId, status, null, productId, categoryId,
                "createdAt", "desc");
        verify(generateInvoicesReportPdfUseCase).execute(invoices, "SIMPLE");
    }

    @Test
    @DisplayName("should return invoices report PDF without optional params")
    void should_get_invoices_report_pdf_without_optional_params() {
        byte[] pdfContent = "PDF no filters".getBytes();
        List<InvoiceListResponse> invoices = List.of();

        when(getInvoicesUseCase.execute(null, null, null, null, null, null, null,
                "createdAt", "desc")).thenReturn(invoices);
        when(generateInvoicesReportPdfUseCase.execute(invoices, "SIMPLE")).thenReturn(pdfContent);

        ResponseEntity<byte[]> result = controller.getInvoicesReportPdf(
                null, null, null, null, null, null, "SIMPLE");

        assertArrayEquals(pdfContent, result.getBody());
        verify(generateInvoicesReportPdfUseCase).execute(invoices, "SIMPLE");
    }
}
