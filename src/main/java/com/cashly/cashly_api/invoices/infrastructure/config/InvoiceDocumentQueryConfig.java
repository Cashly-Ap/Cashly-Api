package com.cashly.cashly_api.invoices.infrastructure.config;

import com.cashly.cashly_api.invoices.application.api.InvoiceDocumentQuery;
import com.cashly.cashly_api.invoices.application.services.InvoiceDocumentQueryService;
import com.cashly.cashly_api.invoices.application.usecases.GetInvoiceByIdUseCase;
import com.cashly.cashly_api.invoices.application.usecases.GetInvoicesUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InvoiceDocumentQueryConfig {
    @Bean
    public InvoiceDocumentQuery invoiceDocumentQuery(GetInvoiceByIdUseCase invoiceQuery, GetInvoicesUseCase invoicesQuery) {
        return new InvoiceDocumentQueryService(invoiceQuery, invoicesQuery);
    }
}
