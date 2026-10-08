package com.cashly.cashly_api.invoices.infrastructure.config;

import com.cashly.cashly_api.invoices.application.api.OrderInvoiceCreationApi;
import com.cashly.cashly_api.invoices.application.services.OrderInvoiceCreationService;
import com.cashly.cashly_api.invoices.application.usecases.CreateInvoiceUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderInvoiceCreationConfig {
    @Bean
    public OrderInvoiceCreationApi orderInvoiceCreationApi(CreateInvoiceUseCase createInvoice) {
        return new OrderInvoiceCreationService(createInvoice);
    }
}
