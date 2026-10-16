package com.cashly.cashly_api.invoices.infrastructure.config;

import com.cashly.cashly_api.invoices.application.ports.in.QuoteInvoiceCreationApi;
import com.cashly.cashly_api.invoices.application.ports.out.ActorIdentityGateway;
import com.cashly.cashly_api.invoices.application.ports.out.CustomerCreditTermsGateway;
import com.cashly.cashly_api.invoices.application.issue.services.IssueInvoiceService;
import com.cashly.cashly_api.invoices.application.create.from_quote.services.QuoteInvoiceCreationService;
import com.cashly.cashly_api.invoices.domain.repository.InvoiceRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuoteInvoiceCreationConfig {
    @Bean
    public QuoteInvoiceCreationApi quoteInvoiceCreationApi(InvoiceRepository invoices, ActorIdentityGateway actors,
                                                          CustomerCreditTermsGateway creditTerms,
                                                          IssueInvoiceService issuance) {
        return new QuoteInvoiceCreationService(invoices, actors, creditTerms, issuance);
    }
}
