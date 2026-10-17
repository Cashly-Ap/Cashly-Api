package com.cashly.cashly_api.finance.billing.invoices.infrastructure.config;

import com.cashly.cashly_api.finance.billing.invoices.application.ports.in.QuoteInvoiceCreationApi;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.ActorIdentityGateway;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.CustomerCreditTermsGateway;
import com.cashly.cashly_api.finance.billing.invoices.application.issue.services.IssueInvoiceService;
import com.cashly.cashly_api.finance.billing.invoices.application.create.from_quote.services.QuoteInvoiceCreationService;
import com.cashly.cashly_api.finance.billing.invoices.domain.repository.InvoiceRepository;
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
