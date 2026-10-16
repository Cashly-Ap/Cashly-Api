package com.cashly.cashly_api.quotes.infrastructure.config;

import com.cashly.cashly_api.quotes.application.ports.in.QuoteDocumentQuery;
import com.cashly.cashly_api.quotes.application.queries.services.QuoteDocumentQueryService;
import com.cashly.cashly_api.quotes.application.list.use_case.GetQuotesUseCase;
import com.cashly.cashly_api.quotes.domain.repository.QuoteRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuoteDocumentQueryConfig {
    @Bean
    public QuoteDocumentQuery quoteDocumentQuery(QuoteRepository repository, GetQuotesUseCase listQuery) {
        return new QuoteDocumentQueryService(repository, listQuery);
    }
}
