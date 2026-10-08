package com.cashly.cashly_api.invoices.infrastructure.config;

import com.cashly.cashly_api.invoices.application.api.QuoteInvoiceCreationApi;
import com.cashly.cashly_api.invoices.application.contracts.ActorIdentityGateway;
import com.cashly.cashly_api.invoices.application.contracts.CustomerCreditTermsGateway;
import com.cashly.cashly_api.invoices.application.contracts.StockDeductionGateway;
import com.cashly.cashly_api.invoices.application.services.InvoiceAuthorizationService;
import com.cashly.cashly_api.invoices.application.services.QuoteInvoiceCreationService;
import com.cashly.cashly_api.invoices.domain.repositories.InvoiceRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuoteInvoiceCreationConfig {
    @Bean
    public QuoteInvoiceCreationApi quoteInvoiceCreationApi(InvoiceRepository invoices, ActorIdentityGateway actors,
                                                          CustomerCreditTermsGateway creditTerms,
                                                          InvoiceAuthorizationService authorization,
                                                          StockDeductionGateway inventory) {
        return new QuoteInvoiceCreationService(invoices, actors, creditTerms, authorization, inventory);
    }
}
