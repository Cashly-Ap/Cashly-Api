package com.cashly.cashly_api.finance.billing.invoices.infrastructure.config;

import com.cashly.cashly_api.finance.billing.invoices.application.ports.in.OrderInvoiceCreationApi;
import com.cashly.cashly_api.finance.billing.invoices.application.create.from_order.services.OrderInvoiceCreationService;
import com.cashly.cashly_api.finance.billing.invoices.application.create.use_case.CreateInvoiceUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderInvoiceCreationConfig {
    @Bean
    public OrderInvoiceCreationApi orderInvoiceCreationApi(
            CreateInvoiceUseCase createInvoice) {
        return new OrderInvoiceCreationService(createInvoice);
    }
}
