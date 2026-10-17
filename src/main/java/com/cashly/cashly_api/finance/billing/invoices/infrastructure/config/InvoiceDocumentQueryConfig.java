package com.cashly.cashly_api.finance.billing.invoices.infrastructure.config;

import com.cashly.cashly_api.finance.billing.invoices.application.ports.in.InvoiceDocumentQuery;
import com.cashly.cashly_api.finance.billing.invoices.application.queries.services.InvoiceDocumentQueryService;
import com.cashly.cashly_api.finance.billing.invoices.application.get.use_case.GetInvoiceByIdUseCase;
import com.cashly.cashly_api.finance.billing.invoices.application.list.use_case.GetInvoicesUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InvoiceDocumentQueryConfig {
    @Bean
    public InvoiceDocumentQuery invoiceDocumentQuery(GetInvoiceByIdUseCase invoiceQuery, GetInvoicesUseCase invoicesQuery) {
        return new InvoiceDocumentQueryService(invoiceQuery, invoicesQuery);
    }
}
