package com.cashly.cashly_api.sales.invoices.invoices.infrastructure.config;

import com.cashly.cashly_api.sales.invoices.invoices.application.ports.in.InvoiceDocumentQuery;
import com.cashly.cashly_api.sales.invoices.invoices.application.queries.services.InvoiceDocumentQueryService;
import com.cashly.cashly_api.sales.invoices.invoices.application.get.use_case.GetInvoiceByIdUseCase;
import com.cashly.cashly_api.sales.invoices.invoices.domain.repository.InvoiceRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InvoiceDocumentQueryConfig {
    @Bean
    public InvoiceDocumentQuery invoiceDocumentQuery(
            GetInvoiceByIdUseCase invoiceQuery,
            InvoiceRepository invoicesQuery) {
        return new InvoiceDocumentQueryService(invoiceQuery, invoicesQuery);
    }
}
