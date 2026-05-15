package com.parsun.parsun_api.reports.quotes.infrastructure.config;

import com.parsun.parsun_api.quotes.domain.repositories.QuoteRepository;
import com.parsun.parsun_api.quotes.application.usecases.GetQuotesUseCase;
import com.parsun.parsun_api.reports.quotes.application.port.QuotePdfPort;
import com.parsun.parsun_api.reports.quotes.application.port.QuotesReportPdfPort;
import com.parsun.parsun_api.reports.quotes.application.usecases.GenerateQuotePdfUseCase;
import com.parsun.parsun_api.reports.quotes.application.usecases.GenerateQuotePdfUseCaseImpl;
import com.parsun.parsun_api.reports.quotes.application.usecases.GenerateQuotesReportPdfUseCase;
import com.parsun.parsun_api.reports.quotes.application.usecases.GenerateQuotesReportPdfUseCaseImpl;
import com.parsun.parsun_api.reports.quotes.infrastructure.pdf.QuotePdfGenerator;
import com.parsun.parsun_api.reports.quotes.infrastructure.pdf.QuotesReportPdfGenerator;
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
