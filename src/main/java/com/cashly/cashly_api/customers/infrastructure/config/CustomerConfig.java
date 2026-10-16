package com.cashly.cashly_api.customers.infrastructure.config;

import com.cashly.cashly_api.customers.profile.application.mappers.CustomerMapper;
import com.cashly.cashly_api.customers.credit.application.ports.out.CreditValidationGateway;
import com.cashly.cashly_api.customers.balance.application.ports.out.CustomerActorGateway;
import com.cashly.cashly_api.customers.balance.application.ports.out.CustomerCashGateway;
import com.cashly.cashly_api.customers.balance.application.add.use_case.AddToCustomerBalanceUseCase;
import com.cashly.cashly_api.customers.balance.application.consume.use_case.ConsumeCustomerBalanceUseCase;
import com.cashly.cashly_api.customers.balance.application.advance.use_case.CreateCustomerAdvanceUseCase;
import com.cashly.cashly_api.customers.profile.application.delete.use_case.DeleteCustomerUseCase;
import com.cashly.cashly_api.customers.balance.application.get.use_case.GetCustomerBalanceUseCase;
import com.cashly.cashly_api.customers.profile.application.get.use_case.GetCustomerUseCase;
import com.cashly.cashly_api.customers.warehouse_access.application.grant.use_case.GrantWarehouseAccessUseCase;
import com.cashly.cashly_api.customers.warehouse_access.application.list.use_case.ListCustomerWarehousesUseCase;
import com.cashly.cashly_api.customers.profile.application.list.use_case.ListCustomersUseCase;
import com.cashly.cashly_api.customers.balance.application.refund.use_case.RefundCustomerBalanceUseCase;
import com.cashly.cashly_api.customers.profile.application.register.use_case.RegisterCustomerUseCase;
import com.cashly.cashly_api.customers.warehouse_access.application.revoke.use_case.RevokeWarehouseAccessUseCase;
import com.cashly.cashly_api.customers.profile.application.update.use_case.UpdateCustomerUseCase;
import com.cashly.cashly_api.customers.credit.application.status.use_case.GetCustomerCreditStatusUseCase;
import com.cashly.cashly_api.customers.credit.application.overdue.use_case.GetOverdueInvoicesUseCase;
import com.cashly.cashly_api.customers.balance.domain.repository.CustomerBalanceRepository;
import com.cashly.cashly_api.customers.profile.domain.repository.CustomerRepository;
import com.cashly.cashly_api.customers.warehouse_access.domain.repository.CustomerWarehouseAccessRepository;
import com.cashly.cashly_api.customers.profile.domain.services.CustomerCodeGenerator;
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
