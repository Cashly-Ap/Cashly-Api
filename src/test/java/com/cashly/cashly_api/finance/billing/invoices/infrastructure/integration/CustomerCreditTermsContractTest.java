package com.cashly.cashly_api.finance.billing.invoices.infrastructure.integration;

import com.cashly.cashly_api.customers.profile.application.queries.services.RepositoryCustomerCreditTermsQuery;
import com.cashly.cashly_api.customers.profile.domain.entity.Customer;
import com.cashly.cashly_api.customers.profile.domain.repository.CustomerRepository;
import com.cashly.cashly_api.customers.profile.domain.valueobjects.CreditDays;
import com.cashly.cashly_api.customers.profile.domain.valueobjects.CustomerId;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerCreditTermsContractTest {
    private static final int CREDIT_DAYS = 15;
    private final CustomerRepository customers = mock(CustomerRepository.class);
    private final LocalCustomerCreditTermsGateway gateway = new LocalCustomerCreditTermsGateway(
            new RepositoryCustomerCreditTermsQuery(customers));

    @Test
    void should_read_credit_days_from_the_customer_provider() {
        UUID customerId = UUID.randomUUID();
        Customer customer = mock(Customer.class);
        when(customer.getCreditDays()).thenReturn(new CreditDays(CREDIT_DAYS));
        when(customers.findById(new CustomerId(customerId))).thenReturn(Optional.of(customer));
        assertEquals(CREDIT_DAYS, gateway.creditDays(customerId));
        verify(customers).findById(new CustomerId(customerId));
    }

    @Test
    void should_preserve_the_missing_customer_error() {
        UUID customerId = UUID.randomUUID();
        when(customers.findById(new CustomerId(customerId))).thenReturn(Optional.empty());
        assertEquals("Customer not found: " + customerId,
                assertThrows(IllegalArgumentException.class, () -> gateway.creditDays(customerId)).getMessage());
    }
}
