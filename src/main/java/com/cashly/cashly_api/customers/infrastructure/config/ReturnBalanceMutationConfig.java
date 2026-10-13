package com.cashly.cashly_api.customers.infrastructure.config;
import com.cashly.cashly_api.customers.application.ports.in.ReturnBalanceMutation;
import com.cashly.cashly_api.customers.application.services.ReturnBalanceMutationService;
import com.cashly.cashly_api.customers.application.usecases.AddToCustomerBalanceUseCase;
import com.cashly.cashly_api.customers.application.usecases.ConsumeCustomerBalanceUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class ReturnBalanceMutationConfig {
    @Bean
    public ReturnBalanceMutation returnBalanceMutation(AddToCustomerBalanceUseCase add, ConsumeCustomerBalanceUseCase consume) {
        return new ReturnBalanceMutationService(add, consume);
    }
}
