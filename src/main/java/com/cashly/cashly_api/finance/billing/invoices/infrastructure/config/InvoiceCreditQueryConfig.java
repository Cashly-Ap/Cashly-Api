package com.cashly.cashly_api.finance.billing.invoices.infrastructure.config;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.in.InvoiceCreditLedgerQuery;
import com.cashly.cashly_api.finance.billing.invoices.application.queries.services.RepositoryInvoiceCreditLedgerQuery;
import com.cashly.cashly_api.finance.billing.invoices.domain.repository.InvoiceRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class InvoiceCreditQueryConfig {
    @Bean
    public InvoiceCreditLedgerQuery invoiceCreditLedgerQuery(InvoiceRepository invoices) {
        return new RepositoryInvoiceCreditLedgerQuery(invoices);
    }
}
