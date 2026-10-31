package com.cashly.cashly_api.finance.billing.invoices.infrastructure.config;

import com.cashly.cashly_api.finance.billing.invoices.application.ports.in.InvoiceReturnAdjustments;
import com.cashly.cashly_api.finance.billing.invoices.application.queries.services.RepositoryInvoiceReturnAdjustments;
import com.cashly.cashly_api.finance.billing.invoices.domain.repository.InvoiceRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InvoiceReturnAdjustmentConfig {
    @Bean
    public InvoiceReturnAdjustments invoiceReturnAdjustments(
            InvoiceRepository invoiceRepository) {
        return new RepositoryInvoiceReturnAdjustments(invoiceRepository);
    }
}
