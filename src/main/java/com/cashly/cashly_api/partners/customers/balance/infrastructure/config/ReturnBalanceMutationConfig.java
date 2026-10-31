package com.cashly.cashly_api.partners.customers.balance.infrastructure.config;
import com.cashly.cashly_api.partners.customers.balance.application.ports.in.ReturnBalanceMutation;
import com.cashly.cashly_api.partners.customers.balance.application.services.ReturnBalanceMutationService;
import com.cashly.cashly_api.partners.customers.balance.application.add.use_case.AddToCustomerBalanceUseCase;
import com.cashly.cashly_api.partners.customers.balance.application.consume.use_case.ConsumeCustomerBalanceUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class ReturnBalanceMutationConfig {
    @Bean
    public ReturnBalanceMutation returnBalanceMutation(
            AddToCustomerBalanceUseCase add,
            ConsumeCustomerBalanceUseCase consume) {
        return new ReturnBalanceMutationService(add, consume);
    }
}
