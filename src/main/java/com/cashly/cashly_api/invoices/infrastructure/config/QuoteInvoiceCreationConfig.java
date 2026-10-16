package com.cashly.cashly_api.invoices.infrastructure.config;

import com.cashly.cashly_api.invoices.application.ports.in.QuoteInvoiceCreationApi;
import com.cashly.cashly_api.invoices.application.ports.out.ActorIdentityGateway;
import com.cashly.cashly_api.invoices.application.ports.out.CustomerCreditTermsGateway;
import com.cashly.cashly_api.invoices.application.ports.out.StockDeductionGateway;
import com.cashly.cashly_api.invoices.application.services.InvoiceAuthorizationService;
import com.cashly.cashly_api.invoices.application.create.from_quote.QuoteInvoiceCreationService;
import com.cashly.cashly_api.invoices.domain.InvoiceRepository;
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
