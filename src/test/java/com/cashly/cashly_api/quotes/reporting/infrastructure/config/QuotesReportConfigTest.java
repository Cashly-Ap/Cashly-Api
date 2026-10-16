package com.cashly.cashly_api.quotes.reporting.infrastructure.config;

import com.cashly.cashly_api.quotes.reporting.application.ports.out.QuoteReportQueryGateway;
import com.cashly.cashly_api.quotes.reporting.application.ports.out.QuotePdfPort;
import com.cashly.cashly_api.quotes.reporting.application.ports.out.QuotesReportPdfPort;
import com.cashly.cashly_api.quotes.reporting.application.generate_quote_pdf.use_case.GenerateQuotePdfUseCase;
import com.cashly.cashly_api.quotes.reporting.application.generate_quotes_report_pdf.use_case.GenerateQuotesReportPdfUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
@DisplayName("QuotesReportConfig Unit Tests")
class QuotesReportConfigTest {

    @Mock
    private QuoteReportQueryGateway query;

    @Mock
    private QuotePdfPort quotePdfPort;

    @Mock
    private QuotesReportPdfPort quotesReportPdfPort;

    @Test
    @DisplayName("should create use case beans")
    void should_create_use_case_beans() {
        var config = new QuotesReportConfig();

        var quotePdfUseCase = config.generateQuotePdfUseCase(query, quotePdfPort);
        assertNotNull(quotePdfUseCase);

        var reportPdfUseCase = config.generateQuotesReportPdfUseCase(query, quotesReportPdfPort);
        assertNotNull(reportPdfUseCase);
    }

    @Test
    @DisplayName("should create port beans")
    void should_create_port_beans() {
        var config = new QuotesReportConfig();

        assertNotNull(config.quotePdfPort());
        assertNotNull(config.quotesReportPdfPort());
    }
}
