package com.parsun.parsun_api.customers.infrastructure.config;

import com.parsun.parsun_api.customers.application.mappers.CustomerMapper;
import com.parsun.parsun_api.customers.application.usecases.*;
import com.parsun.parsun_api.customers.domain.repositories.CustomerBalanceRepository;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import com.parsun.parsun_api.customers.domain.repositories.CustomerWarehouseAccessRepository;
import com.parsun.parsun_api.customers.domain.services.CustomerCodeGenerator;
import com.parsun.parsun_api.customers.infrastructure.services.SequentialCustomerCodeGenerator;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class CustomerConfig {

    @Bean
    public CustomerMapper customerMapper() {
        return new CustomerMapper();
    }

    @Bean
    public CustomerCodeGenerator customerCodeGenerator(CustomerRepository customerRepository) {
        return new SequentialCustomerCodeGenerator(customerRepository);
    }

    @Bean
    public RegisterCustomerUseCase registerCustomerUseCase(CustomerRepository customerRepository,
                                                             CustomerCodeGenerator customerCodeGenerator,
                                                             CustomerMapper customerMapper,
                                                             PasswordEncoder passwordEncoder) {
        return new RegisterCustomerUseCase(customerRepository, customerCodeGenerator, customerMapper, passwordEncoder);
    }

    @Bean
    public GetCustomerUseCase getCustomerUseCase(CustomerRepository customerRepository,
                                                   CustomerMapper customerMapper) {
        return new GetCustomerUseCase(customerRepository, customerMapper);
    }

    @Bean
    public ListCustomersUseCase listCustomersUseCase(CustomerRepository customerRepository,
                                                       CustomerMapper customerMapper) {
        return new ListCustomersUseCase(customerRepository, customerMapper);
    }

    @Bean
    public UpdateCustomerUseCase updateCustomerUseCase(CustomerRepository customerRepository,
                                                         CustomerMapper customerMapper,
                                                         PasswordEncoder passwordEncoder) {
        return new UpdateCustomerUseCase(customerRepository, customerMapper, passwordEncoder);
    }

    @Bean
    public DeleteCustomerUseCase deleteCustomerUseCase(CustomerRepository customerRepository) {
        return new DeleteCustomerUseCase(customerRepository);
    }

    @Bean
    public ChangeCustomerPortalPasswordUseCase changeCustomerPortalPasswordUseCase(
            CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder) {
        return new ChangeCustomerPortalPasswordUseCase(customerRepository, passwordEncoder);
    }

    @Bean
    public GrantWarehouseAccessUseCase grantWarehouseAccessUseCase(
            CustomerWarehouseAccessRepository accessRepository,
            CustomerRepository customerRepository,
            WarehouseRepository warehouseRepository) {
        return new GrantWarehouseAccessUseCase(accessRepository, customerRepository, warehouseRepository);
    }

    @Bean
    public RevokeWarehouseAccessUseCase revokeWarehouseAccessUseCase(
            CustomerWarehouseAccessRepository accessRepository) {
        return new RevokeWarehouseAccessUseCase(accessRepository);
    }

    @Bean
    public ListCustomerWarehousesUseCase listCustomerWarehousesUseCase(
            CustomerWarehouseAccessRepository accessRepository,
            CustomerRepository customerRepository,
            WarehouseRepository warehouseRepository) {
        return new ListCustomerWarehousesUseCase(accessRepository, customerRepository, warehouseRepository);
    }

    @Bean
    public GetCustomerBalanceUseCase getCustomerBalanceUseCase(
            CustomerBalanceRepository customerBalanceRepository) {
        return new GetCustomerBalanceUseCase(customerBalanceRepository);
    }

    @Bean
    public AddToCustomerBalanceUseCase addToCustomerBalanceUseCase(
            CustomerBalanceRepository customerBalanceRepository) {
        return new AddToCustomerBalanceUseCase(customerBalanceRepository);
    }

    @Bean
    public ConsumeCustomerBalanceUseCase consumeCustomerBalanceUseCase(
            CustomerBalanceRepository customerBalanceRepository) {
        return new ConsumeCustomerBalanceUseCase(customerBalanceRepository);
    }
}
