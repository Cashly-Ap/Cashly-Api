package com.cashly.cashly_api.cash_register.sessions.infrastructure.config;
import com.cashly.cashly_api.cash_register.sessions.application.ports.in.CashRegisterOperations;
import com.cashly.cashly_api.cash_register.sessions.application.services.CashRegisterOperationsService;
import com.cashly.cashly_api.cash_register.sessions.application.movements.CashMovementService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class CashRegisterOperationsConfig {
    @Bean
    public CashRegisterOperations cashRegisterOperations(CashMovementService movements) {
        return new CashRegisterOperationsService(movements);
    }
}
