package com.cashly.cashly_api.customers.infrastructure.config;

import com.cashly.cashly_api.customers.profile.application.mappers.CustomerMapper;
import com.cashly.cashly_api.customers.credit.application.ports.out.CreditValidationGateway;
import com.cashly.cashly_api.customers.balance.application.ports.out.CustomerActorGateway;
import com.cashly.cashly_api.customers.balance.application.ports.out.CustomerCashGateway;
import com.cashly.cashly_api.customers.balance.application.add.AddToCustomerBalanceUseCase;
import com.cashly.cashly_api.customers.balance.application.consume.ConsumeCustomerBalanceUseCase;
import com.cashly.cashly_api.customers.balance.application.advance.CreateCustomerAdvanceUseCase;
import com.cashly.cashly_api.customers.profile.application.delete.DeleteCustomerUseCase;
import com.cashly.cashly_api.customers.balance.application.get.GetCustomerBalanceUseCase;
import com.cashly.cashly_api.customers.profile.application.get.GetCustomerUseCase;
import com.cashly.cashly_api.customers.warehouse_access.application.grant.GrantWarehouseAccessUseCase;
import com.cashly.cashly_api.customers.warehouse_access.application.list.ListCustomerWarehousesUseCase;
import com.cashly.cashly_api.customers.profile.application.list.ListCustomersUseCase;
import com.cashly.cashly_api.customers.balance.application.refund.RefundCustomerBalanceUseCase;
import com.cashly.cashly_api.customers.profile.application.register.RegisterCustomerUseCase;
import com.cashly.cashly_api.customers.warehouse_access.application.revoke.RevokeWarehouseAccessUseCase;
import com.cashly.cashly_api.customers.profile.application.update.UpdateCustomerUseCase;
import com.cashly.cashly_api.customers.credit.application.status.GetCustomerCreditStatusUseCase;
import com.cashly.cashly_api.customers.credit.application.overdue.GetOverdueInvoicesUseCase;
import com.cashly.cashly_api.customers.balance.domain.CustomerBalanceRepository;
import com.cashly.cashly_api.customers.profile.domain.CustomerRepository;
import com.cashly.cashly_api.customers.warehouse_access.domain.CustomerWarehouseAccessRepository;
import com.cashly.cashly_api.customers.profile.domain.CustomerCodeGenerator;
import com.cashly.cashly_api.customers.profile.infrastructure.services.SequentialCustomerCodeGenerator;
import com.cashly.cashly_api.customers.warehouse_access.application.ports.out.WarehouseDirectoryGateway;
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
                                                             CustomerMapper customerMapper,
                                                             CustomerWarehouseAccessRepository warehouseAccessRepository,
                                                             WarehouseDirectoryGateway warehouseDirectory) {
        return new RegisterCustomerUseCase(customerRepository, customerCodeGenerator, customerMapper,
                warehouseAccessRepository, warehouseDirectory);
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

    @Bean
    public GrantWarehouseAccessUseCase grantWarehouseAccessUseCase(
            CustomerWarehouseAccessRepository accessRepository,
            CustomerRepository customerRepository,
            WarehouseDirectoryGateway warehouseDirectory) {
        return new GrantWarehouseAccessUseCase(accessRepository, customerRepository, warehouseDirectory);
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
            WarehouseDirectoryGateway warehouseDirectory) {
        return new ListCustomerWarehousesUseCase(accessRepository, customerRepository, warehouseDirectory);
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

    @Bean
    public CreateCustomerAdvanceUseCase createCustomerAdvanceUseCase(
            AddToCustomerBalanceUseCase addToCustomerBalanceUseCase,
            CustomerCashGateway cashGateway,
            CustomerActorGateway actorGateway) {
        return new CreateCustomerAdvanceUseCase(
                addToCustomerBalanceUseCase, cashGateway, actorGateway);
    }

    @Bean
    public RefundCustomerBalanceUseCase refundCustomerBalanceUseCase(
            ConsumeCustomerBalanceUseCase consumeCustomerBalanceUseCase,
            CustomerCashGateway cashGateway,
            CustomerActorGateway actorGateway) {
        return new RefundCustomerBalanceUseCase(
                consumeCustomerBalanceUseCase, cashGateway, actorGateway);
    }

    @Bean
    public GetCustomerCreditStatusUseCase getCustomerCreditStatusUseCase(
            CreditValidationGateway creditValidationGateway) {
        return new GetCustomerCreditStatusUseCase(creditValidationGateway);
    }

    @Bean
    public GetOverdueInvoicesUseCase getOverdueInvoicesUseCase(
            CreditValidationGateway creditValidationGateway) {
        return new GetOverdueInvoicesUseCase(creditValidationGateway);
    }
}
