package com.cashly.cashly_api.invoices.infrastructure.config;

import com.cashly.cashly_api.invoices.application.ports.in.InvoiceReturnAdjustments;
import com.cashly.cashly_api.invoices.application.services.RepositoryInvoiceReturnAdjustments;
import com.cashly.cashly_api.invoices.domain.repositories.InvoiceRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InvoiceReturnAdjustmentConfig {
    @Bean
    public InvoiceReturnAdjustments invoiceReturnAdjustments(InvoiceRepository invoiceRepository) {
        return new RepositoryInvoiceReturnAdjustments(invoiceRepository);
    }
}
