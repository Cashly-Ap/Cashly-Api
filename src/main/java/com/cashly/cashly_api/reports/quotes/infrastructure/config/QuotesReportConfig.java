package com.cashly.cashly_api.reports.quotes.infrastructure.config;
import com.cashly.cashly_api.reports.quotes.application.contracts.QuoteReportQueryGateway;
import com.cashly.cashly_api.reports.quotes.application.port.QuotePdfPort;
import com.cashly.cashly_api.reports.quotes.application.port.QuotesReportPdfPort;
import com.cashly.cashly_api.reports.quotes.application.usecases.GenerateQuotePdfUseCase;
import com.cashly.cashly_api.reports.quotes.application.usecases.GenerateQuotePdfUseCaseImpl;
import com.cashly.cashly_api.reports.quotes.application.usecases.GenerateQuotesReportPdfUseCase;
import com.cashly.cashly_api.reports.quotes.application.usecases.GenerateQuotesReportPdfUseCaseImpl;
import com.cashly.cashly_api.reports.quotes.infrastructure.pdf.QuotePdfGenerator;
import com.cashly.cashly_api.reports.quotes.infrastructure.pdf.QuotesReportPdfGenerator;
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