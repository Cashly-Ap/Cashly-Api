package com.cashly.cashly_api.sales.invoices.invoices.infrastructure.config;
import com.cashly.cashly_api.sales.invoices.invoices.application.ports.in.ReturnInvoiceQuery;
import com.cashly.cashly_api.sales.invoices.invoices.application.queries.services.RepositoryReturnInvoiceQuery;
import com.cashly.cashly_api.sales.invoices.invoices.domain.repository.InvoiceRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class ReturnInvoiceQueryConfig {
    @Bean
    public ReturnInvoiceQuery returnInvoiceQuery(InvoiceRepository invoices) {
        return new RepositoryReturnInvoiceQuery(invoices);
    }
}
