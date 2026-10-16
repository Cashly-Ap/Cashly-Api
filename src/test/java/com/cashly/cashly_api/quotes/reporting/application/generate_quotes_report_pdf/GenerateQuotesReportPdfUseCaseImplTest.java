package com.cashly.cashly_api.quotes.reporting.application.generate_quotes_report_pdf;

import com.cashly.cashly_api.quotes.reporting.application.dto.QuoteReportRow;
import com.cashly.cashly_api.quotes.reporting.application.ports.out.QuotesReportPdfPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import com.cashly.cashly_api.quotes.reporting.application.ports.out.QuoteReportQueryGateway;
import com.cashly.cashly_api.quotes.reporting.application.dto.QuoteReportFilter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GenerateQuotesReportPdfUseCaseImpl Unit Tests")
class GenerateQuotesReportPdfUseCaseImplTest {

    @Mock
    private QuotesReportPdfPort quotesReportPdfPort;

    @Mock private QuoteReportQueryGateway query;
    @InjectMocks
    private GenerateQuotesReportPdfUseCaseImpl useCase;

    @Test
    @DisplayName("should generate quotes report PDF with mode")
    void should_generate_quotes_report_pdf() {
        List<QuoteReportRow> quotes = List.of(mock(QuoteReportRow.class));
        var filter = new QuoteReportFilter(null, null, null, null, null, null);
        when(query.findAll(filter)).thenReturn(quotes);
        byte[] expectedPdf = "Quotes report PDF".getBytes();

        when(quotesReportPdfPort.generatePdf(quotes, "SIMPLE")).thenReturn(expectedPdf);

        byte[] result = useCase.execute(filter, "SIMPLE");

        assertArrayEquals(expectedPdf, result);
        verify(quotesReportPdfPort).generatePdf(quotes, "SIMPLE");
    }

    @Test
    @DisplayName("should generate quotes report PDF with null mode defaulting to simple")
    void should_generate_quotes_report_pdf_with_null_mode() {
        List<QuoteReportRow> quotes = List.of();
        var filter = new QuoteReportFilter(null, null, null, null, null, null);
        when(query.findAll(filter)).thenReturn(quotes);
        byte[] expectedPdf = "Default PDF".getBytes();

        when(quotesReportPdfPort.generatePdf(quotes, "simple")).thenReturn(expectedPdf);

        byte[] result = useCase.execute(filter, null);

        assertArrayEquals(expectedPdf, result);
        verify(quotesReportPdfPort).generatePdf(quotes, "simple");
    }
}
