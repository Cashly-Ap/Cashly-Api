package com.cashly.cashly_api.reports.quotes.infrastructure.adapters.in.controllers;

import com.cashly.cashly_api.reports.quotes.application.dto.QuoteReportRow;
import com.cashly.cashly_api.reports.quotes.application.dto.QuoteReportFilter;
import com.cashly.cashly_api.reports.quotes.application.usecases.GenerateQuotePdfUseCase;
import com.cashly.cashly_api.reports.quotes.application.usecases.GenerateQuotesReportPdfUseCase;
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
@DisplayName("QuotesReportController Unit Tests")
class QuotesReportControllerTest {

    @Mock
    private GenerateQuotePdfUseCase generateQuotePdfUseCase;

    @Mock
    private GenerateQuotesReportPdfUseCase generateQuotesReportPdfUseCase;



    @InjectMocks
    private QuotesReportController controller;

    @Test
    @DisplayName("should return quote PDF as attachment")
    void should_get_quote_pdf() {
        UUID quoteId = UUID.randomUUID();
        byte[] pdfContent = "Quote PDF".getBytes();

        when(generateQuotePdfUseCase.execute(quoteId)).thenReturn(pdfContent);

        ResponseEntity<byte[]> result = controller.getQuotePdf(quoteId);

        assertEquals(MediaType.APPLICATION_PDF, result.getHeaders().getContentType());
        assertTrue(result.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION).get(0)
                .contains("presupuesto-" + quoteId + ".pdf"));
        assertArrayEquals(pdfContent, result.getBody());
        assertEquals(200, result.getStatusCodeValue());
        verify(generateQuotePdfUseCase).execute(quoteId);
    }

    @Test
    @DisplayName("should return quotes report PDF as attachment")
    void should_get_quotes_report_pdf() {
        UUID customerId = UUID.randomUUID();
        LocalDate dateFrom = LocalDate.of(2026, 1, 1);
        LocalDate dateTo = LocalDate.of(2026, 12, 31);
        UUID productId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        byte[] pdfContent = "Quotes report PDF".getBytes();
        List<QuoteReportRow> quotes = List.of();

        var filter = new QuoteReportFilter(dateFrom, dateTo, customerId, "PENDING", productId, categoryId);
        when(generateQuotesReportPdfUseCase.execute(filter, "SIMPLE")).thenReturn(pdfContent);

        ResponseEntity<byte[]> result = controller.getQuotesReportPdf(
                dateFrom, dateTo, customerId, "PENDING", productId, categoryId, "SIMPLE");

        assertEquals(MediaType.APPLICATION_PDF, result.getHeaders().getContentType());
        assertTrue(result.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION).get(0)
                .contains("reporte-presupuestos-"));
        assertArrayEquals(pdfContent, result.getBody());

        verify(generateQuotesReportPdfUseCase).execute(filter, "SIMPLE");
    }

    @Test
    @DisplayName("should return quotes report PDF without optional params")
    void should_get_quotes_report_pdf_without_optional_params() {
        byte[] pdfContent = "PDF no filters".getBytes();
        List<QuoteReportRow> quotes = List.of();

        var filter = new QuoteReportFilter(null, null, null, null, null, null);
        when(generateQuotesReportPdfUseCase.execute(filter, "SIMPLE")).thenReturn(pdfContent);

        ResponseEntity<byte[]> result = controller.getQuotesReportPdf(
                null, null, null, null, null, null, "SIMPLE");

        assertArrayEquals(pdfContent, result.getBody());
        verify(generateQuotesReportPdfUseCase).execute(filter, "SIMPLE");
    }
}
