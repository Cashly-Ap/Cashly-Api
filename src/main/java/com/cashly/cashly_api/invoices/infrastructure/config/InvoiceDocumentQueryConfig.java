package com.cashly.cashly_api.invoices.infrastructure.config;

import com.cashly.cashly_api.invoices.application.ports.in.InvoiceDocumentQuery;
import com.cashly.cashly_api.invoices.application.queries.InvoiceDocumentQueryService;
import com.cashly.cashly_api.invoices.application.get.GetInvoiceByIdUseCase;
import com.cashly.cashly_api.invoices.application.list.GetInvoicesUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InvoiceDocumentQueryConfig {
    @Bean
    public InvoiceDocumentQuery invoiceDocumentQuery(GetInvoiceByIdUseCase invoiceQuery, GetInvoicesUseCase invoicesQuery) {
        return new InvoiceDocumentQueryService(invoiceQuery, invoicesQuery);
    }
}
