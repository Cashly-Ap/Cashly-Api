package com.cashly.cashly_api.customers.balance.infrastructure.config;
import com.cashly.cashly_api.customers.balance.application.ports.in.InvoiceBalanceConsumption;
import com.cashly.cashly_api.customers.balance.application.services.InvoiceBalanceConsumptionService;
import com.cashly.cashly_api.customers.balance.application.consume.use_case.ConsumeCustomerBalanceUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class InvoiceBalanceConsumptionConfig {
    @Bean
    public InvoiceBalanceConsumption invoiceBalanceConsumption(ConsumeCustomerBalanceUseCase consumeBalance) {
        return new InvoiceBalanceConsumptionService(consumeBalance);
    }
}
