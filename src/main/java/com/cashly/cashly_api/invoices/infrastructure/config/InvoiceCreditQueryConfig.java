package com.cashly.cashly_api.invoices.infrastructure.config;
import com.cashly.cashly_api.invoices.application.ports.in.InvoiceCreditLedgerQuery;
import com.cashly.cashly_api.invoices.application.queries.RepositoryInvoiceCreditLedgerQuery;
import com.cashly.cashly_api.invoices.domain.InvoiceRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class InvoiceCreditQueryConfig {
    @Bean
    public InvoiceCreditLedgerQuery invoiceCreditLedgerQuery(InvoiceRepository invoices) {
        return new RepositoryInvoiceCreditLedgerQuery(invoices);
    }
}
