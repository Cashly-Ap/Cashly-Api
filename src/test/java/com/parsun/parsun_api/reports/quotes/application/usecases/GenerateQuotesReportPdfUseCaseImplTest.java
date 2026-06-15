package com.parsun.parsun_api.reports.quotes.application.usecases;

import com.parsun.parsun_api.quotes.application.dto.response.QuoteListResponse;
import com.parsun.parsun_api.reports.quotes.application.port.QuotesReportPdfPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GenerateQuotesReportPdfUseCaseImpl Unit Tests")
class GenerateQuotesReportPdfUseCaseImplTest {

    @Mock
    private QuotesReportPdfPort quotesReportPdfPort;

    @InjectMocks
    private GenerateQuotesReportPdfUseCaseImpl useCase;

    @Test
    @DisplayName("should generate quotes report PDF with mode")
    void should_generate_quotes_report_pdf() {
        List<QuoteListResponse> quotes = List.of(mock(QuoteListResponse.class));
        byte[] expectedPdf = "Quotes report PDF".getBytes();

        when(quotesReportPdfPort.generatePdf(quotes, "SIMPLE")).thenReturn(expectedPdf);

        byte[] result = useCase.execute(quotes, "SIMPLE");

        assertArrayEquals(expectedPdf, result);
        verify(quotesReportPdfPort).generatePdf(quotes, "SIMPLE");
    }

    @Test
    @DisplayName("should generate quotes report PDF with null mode defaulting to simple")
    void should_generate_quotes_report_pdf_with_null_mode() {
        List<QuoteListResponse> quotes = List.of();
        byte[] expectedPdf = "Default PDF".getBytes();

        when(quotesReportPdfPort.generatePdf(quotes, "simple")).thenReturn(expectedPdf);

        byte[] result = useCase.execute(quotes, null);

        assertArrayEquals(expectedPdf, result);
        verify(quotesReportPdfPort).generatePdf(quotes, "simple");
    }
}
