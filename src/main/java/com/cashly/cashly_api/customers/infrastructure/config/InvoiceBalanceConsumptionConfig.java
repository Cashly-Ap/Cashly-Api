package com.cashly.cashly_api.customers.infrastructure.config;
import com.cashly.cashly_api.customers.application.ports.in.InvoiceBalanceConsumption;
import com.cashly.cashly_api.customers.application.services.InvoiceBalanceConsumptionService;
import com.cashly.cashly_api.customers.application.usecases.ConsumeCustomerBalanceUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class InvoiceBalanceConsumptionConfig {
    @Bean
    public InvoiceBalanceConsumption invoiceBalanceConsumption(ConsumeCustomerBalanceUseCase consumeBalance) {
        return new InvoiceBalanceConsumptionService(consumeBalance);
    }
}
