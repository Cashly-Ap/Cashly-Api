package com.cashly.cashly_api.customers.balance.infrastructure.config;
import com.cashly.cashly_api.customers.balance.application.ports.in.ReturnBalanceMutation;
import com.cashly.cashly_api.customers.balance.application.services.ReturnBalanceMutationService;
import com.cashly.cashly_api.customers.balance.application.add.AddToCustomerBalanceUseCase;
import com.cashly.cashly_api.customers.balance.application.consume.ConsumeCustomerBalanceUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class ReturnBalanceMutationConfig {
    @Bean
    public ReturnBalanceMutation returnBalanceMutation(AddToCustomerBalanceUseCase add, ConsumeCustomerBalanceUseCase consume) {
        return new ReturnBalanceMutationService(add, consume);
    }
}
