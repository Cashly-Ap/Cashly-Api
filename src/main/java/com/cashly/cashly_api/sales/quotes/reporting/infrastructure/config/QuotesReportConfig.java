package com.cashly.cashly_api.sales.quotes.reporting.infrastructure.config;
import com.cashly.cashly_api.sales.quotes.reporting.application.ports.out.QuoteReportQueryGateway;
import com.cashly.cashly_api.sales.quotes.reporting.application.ports.out.QuotePdfPort;
import com.cashly.cashly_api.sales.quotes.reporting.application.ports.out.QuotesReportPdfPort;
import com.cashly.cashly_api.sales.quotes.reporting.application.generate_quote_pdf.use_case.GenerateQuotePdfUseCase;
import com.cashly.cashly_api.sales.quotes.reporting.application.generate_quote_pdf.services.GenerateQuotePdfUseCaseImpl;
import com.cashly.cashly_api.sales.quotes.reporting.application.generate_quotes_report_pdf.use_case.GenerateQuotesReportPdfUseCase;
import com.cashly.cashly_api.sales.quotes.reporting.application.generate_quotes_report_pdf.services.GenerateQuotesReportPdfUseCaseImpl;
import com.cashly.cashly_api.sales.quotes.reporting.infrastructure.pdf.QuotePdfGenerator;
import com.cashly.cashly_api.sales.quotes.reporting.infrastructure.pdf.QuotesReportPdfGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class QuotesReportConfig {
    @Bean
    public QuotePdfPort quotePdfPort() { return new QuotePdfGenerator(); }
    @Bean
    public QuotesReportPdfPort quotesReportPdfPort() { return new QuotesReportPdfGenerator(); }
    @Bean
    public GenerateQuotePdfUseCase generateQuotePdfUseCase(QuoteReportQueryGateway query, QuotePdfPort pdf) {
        return new GenerateQuotePdfUseCaseImpl(query, pdf);
    }
    @Bean
    public GenerateQuotesReportPdfUseCase generateQuotesReportPdfUseCase(QuoteReportQueryGateway query, QuotesReportPdfPort pdf) {
        return new GenerateQuotesReportPdfUseCaseImpl(query, pdf);
    }
}