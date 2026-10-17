package com.cashly.cashly_api.partners.customers.credit.infrastructure.config;
import com.cashly.cashly_api.partners.customers.credit.application.ports.in.CustomerCreditContextQuery;
import com.cashly.cashly_api.partners.customers.credit.application.queries.services.RepositoryCustomerCreditContextQuery;
import com.cashly.cashly_api.partners.customers.profile.domain.repository.CustomerRepository;
import com.cashly.cashly_api.partners.customers.balance.domain.repository.CustomerBalanceRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class CustomerCreditQueryConfig {
    @Bean
    public CustomerCreditContextQuery customerCreditContextQuery(CustomerRepository customers, CustomerBalanceRepository balances) {
        return new RepositoryCustomerCreditContextQuery(customers, balances);
    }
}
