package com.cashly.cashly_api.quotes.infrastructure.config;

import com.cashly.cashly_api.quotes.application.ports.in.QuoteDocumentQuery;
import com.cashly.cashly_api.quotes.application.services.QuoteDocumentQueryService;
import com.cashly.cashly_api.quotes.application.usecases.GetQuotesUseCase;
import com.cashly.cashly_api.quotes.domain.repositories.QuoteRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuoteDocumentQueryConfig {
    @Bean
    public QuoteDocumentQuery quoteDocumentQuery(QuoteRepository repository, GetQuotesUseCase listQuery) {
        return new QuoteDocumentQueryService(repository, listQuery);
    }
}
