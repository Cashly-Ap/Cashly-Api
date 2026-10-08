package com.cashly.cashly_api.customers.application.services;
import com.cashly.cashly_api.customers.domain.entities.Customer;
import com.cashly.cashly_api.customers.domain.entities.CustomerBalance;
import com.cashly.cashly_api.customers.domain.repositories.CustomerRepository;
import com.cashly.cashly_api.customers.domain.repositories.CustomerBalanceRepository;
import com.cashly.cashly_api.customers.domain.valueobjects.CustomerId;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
class RepositoryCustomerCreditContextQueryTest {
    private final CustomerRepository customers = mock(CustomerRepository.class);
    private final CustomerBalanceRepository balances = mock(CustomerBalanceRepository.class);
    private final RepositoryCustomerCreditContextQuery query = new RepositoryCustomerCreditContextQuery(customers, balances);
    private final UUID id = UUID.randomUUID();
    @Test
    void should_project_customer_and_default_absent_balance_to_zero() {
        var customer = mock(Customer.class, RETURNS_DEEP_STUBS);
        when(customer.getLegalName().getValue()).thenReturn("Cashly Customer");
        when(customer.getRif().getValue()).thenReturn("J-12345678-9");
        when(customer.getCreditLimit().getValue()).thenReturn(new BigDecimal("500"));
        when(customers.findById(new CustomerId(id))).thenReturn(Optional.of(customer));
        var result = query.findById(id).orElseThrow();
        assertThat(result.customerId()).isEqualTo(id);
        assertThat(result.name()).isEqualTo("Cashly Customer");
        assertThat(result.rif()).isEqualTo("J-12345678-9");
        assertThat(result.creditLimit()).isEqualByComparingTo("500");
        assertThat(result.balance()).isEqualByComparingTo("0");
    }
    @Test
    void should_project_existing_customer_balance() {
        var customer = mock(Customer.class, RETURNS_DEEP_STUBS);
        var balance = mock(CustomerBalance.class);
        when(balance.getTotalBalance()).thenReturn(new BigDecimal("125"));
        when(customers.findById(new CustomerId(id))).thenReturn(Optional.of(customer));
        when(balances.findByCustomerId(id)).thenReturn(Optional.of(balance));
        assertThat(query.findById(id).orElseThrow().balance()).isEqualByComparingTo("125");
    }
    @Test
    void should_not_query_balance_for_missing_customer() {
        assertThat(query.findById(id)).isEmpty();
        verifyNoInteractions(balances);
    }
}
