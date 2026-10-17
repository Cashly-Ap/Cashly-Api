package com.cashly.cashly_api.sales.quotes.infrastructure.config;

import com.cashly.cashly_api.sales.quotes.application.ports.in.QuoteDocumentQuery;
import com.cashly.cashly_api.sales.quotes.application.queries.services.QuoteDocumentQueryService;
import com.cashly.cashly_api.sales.quotes.application.list.use_case.GetQuotesUseCase;
import com.cashly.cashly_api.sales.quotes.domain.repository.QuoteRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuoteDocumentQueryConfig {
    @Bean
    public QuoteDocumentQuery quoteDocumentQuery(QuoteRepository repository, GetQuotesUseCase listQuery) {
        return new QuoteDocumentQueryService(repository, listQuery);
    }
}
