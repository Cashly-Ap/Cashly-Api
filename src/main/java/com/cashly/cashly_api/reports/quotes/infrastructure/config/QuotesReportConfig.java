package com.cashly.cashly_api.reports.quotes.infrastructure.config;

import com.cashly.cashly_api.quotes.domain.repositories.QuoteRepository;
import com.cashly.cashly_api.quotes.application.usecases.GetQuotesUseCase;
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
    public QuotePdfPort quotePdfPort() {
        return new QuotePdfGenerator();
    }

    @Bean
    public QuotesReportPdfPort quotesReportPdfPort() {
        return new QuotesReportPdfGenerator();
    }

    @Bean
    public GenerateQuotePdfUseCase generateQuotePdfUseCase(
            QuoteRepository quoteRepository,
            QuotePdfPort quotePdfPort) {
        return new GenerateQuotePdfUseCaseImpl(quoteRepository, quotePdfPort);
    }

    @Bean
    public GenerateQuotesReportPdfUseCase generateQuotesReportPdfUseCase(
            QuotesReportPdfPort quotesReportPdfPort) {
        return new GenerateQuotesReportPdfUseCaseImpl(quotesReportPdfPort);
    }
}
