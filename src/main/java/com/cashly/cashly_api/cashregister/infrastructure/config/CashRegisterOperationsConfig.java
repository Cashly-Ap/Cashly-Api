package com.cashly.cashly_api.cashregister.infrastructure.config;
import com.cashly.cashly_api.cashregister.application.api.CashRegisterOperations;
import com.cashly.cashly_api.cashregister.application.services.CashRegisterOperationsService;
import com.cashly.cashly_api.cashregister.application.services.CashMovementService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class CashRegisterOperationsConfig {
    @Bean
    public CashRegisterOperations cashRegisterOperations(CashMovementService movements) {
        return new CashRegisterOperationsService(movements);
    }
}
