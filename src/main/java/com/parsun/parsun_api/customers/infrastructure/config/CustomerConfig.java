package com.parsun.parsun_api.customers.infrastructure.config;

import com.parsun.parsun_api.customers.application.mappers.CustomerMapper;
import com.parsun.parsun_api.customers.application.usecases.*;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import com.parsun.parsun_api.customers.domain.services.CustomerCodeGenerator;
import com.parsun.parsun_api.customers.infrastructure.services.SequentialCustomerCodeGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
                                                            CustomerMapper customerMapper) {
        return new RegisterCustomerUseCase(customerRepository, customerCodeGenerator, customerMapper);
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
                                                        CustomerMapper customerMapper) {
        return new UpdateCustomerUseCase(customerRepository, customerMapper);
    }

    @Bean
    public DeleteCustomerUseCase deleteCustomerUseCase(CustomerRepository customerRepository) {
        return new DeleteCustomerUseCase(customerRepository);
    }
}
