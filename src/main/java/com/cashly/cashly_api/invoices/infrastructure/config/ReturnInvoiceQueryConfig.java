package com.cashly.cashly_api.invoices.infrastructure.config;
import com.cashly.cashly_api.invoices.application.ports.in.ReturnInvoiceQuery;
import com.cashly.cashly_api.invoices.application.queries.RepositoryReturnInvoiceQuery;
import com.cashly.cashly_api.invoices.domain.InvoiceRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class ReturnInvoiceQueryConfig {
    @Bean
    public ReturnInvoiceQuery returnInvoiceQuery(InvoiceRepository invoices) { return new RepositoryReturnInvoiceQuery(invoices); }
}
