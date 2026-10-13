package com.cashly.cashly_api.customers.infrastructure.config;
import com.cashly.cashly_api.customers.application.ports.in.CustomerCreditContextQuery;
import com.cashly.cashly_api.customers.application.services.RepositoryCustomerCreditContextQuery;
import com.cashly.cashly_api.customers.domain.repositories.CustomerRepository;
import com.cashly.cashly_api.customers.domain.repositories.CustomerBalanceRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class CustomerCreditQueryConfig {
    @Bean
    public CustomerCreditContextQuery customerCreditContextQuery(CustomerRepository customers, CustomerBalanceRepository balances) {
        return new RepositoryCustomerCreditContextQuery(customers, balances);
    }
}
