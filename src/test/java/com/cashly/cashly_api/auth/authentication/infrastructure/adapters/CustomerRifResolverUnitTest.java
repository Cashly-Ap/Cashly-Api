package com.cashly.cashly_api.auth.authentication.infrastructure.adapters;

import com.cashly.cashly_api.auth.authentication.domain.entities.Authenticable;
import com.cashly.cashly_api.auth.authentication.domain.entities.AuthenticableType;
import com.cashly.cashly_api.customers.domain.entities.Customer;
import com.cashly.cashly_api.customers.domain.repositories.CustomerRepository;
import com.cashly.cashly_api.customers.domain.valueobjects.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerRifResolver Unit Tests")
class CustomerRifResolverUnitTest {

    @Mock
    private CustomerRepository customerRepository;

    private CustomerRifResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new CustomerRifResolver(customerRepository);
    }

    @Test
    @DisplayName("should_ReturnEmpty_When_ResolveByEmailCalled")
    void should_ReturnEmpty_When_ResolveByEmailCalled() {
        Optional<Authenticable> result = resolver.resolveByEmail("any@email.com");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("should_ReturnAuthenticable_When_CustomerFoundByRif")
    void should_ReturnAuthenticable_When_CustomerFoundByRif() {
        Rif rif = new Rif("J-12345678-9");
        Customer customer = createTestCustomer(rif, true, true);

        when(customerRepository.findActivePortalCustomerByRif(any(Rif.class))).thenReturn(Optional.of(customer));

        Optional<Authenticable> result = resolver.resolveByRif(rif);

        assertTrue(result.isPresent());
        assertEquals(AuthenticableType.CUSTOMER, result.get().getType());
        assertEquals(customer.getId().getValue().toString(), result.get().getSubjectId());
        assertEquals(customer.getLegalName().getValue(), result.get().getUserName());
        assertEquals(customer.getPortalPassword(), result.get().getHashedPassword());
        assertTrue(result.get().canAuthenticate());
        verify(customerRepository, times(1)).findActivePortalCustomerByRif(rif);
    }

    @Test
    @DisplayName("should_ReturnEmpty_When_CustomerNotFoundByRif")
    void should_ReturnEmpty_When_CustomerNotFoundByRif() {
        Rif rif = new Rif("J-87654321-0");

        when(customerRepository.findActivePortalCustomerByRif(any(Rif.class))).thenReturn(Optional.empty());

        Optional<Authenticable> result = resolver.resolveByRif(rif);

        assertTrue(result.isEmpty());
        verify(customerRepository, times(1)).findActivePortalCustomerByRif(rif);
    }

    @Test
    @DisplayName("should_WrapCustomerInAdapter_When_Found")
    void should_WrapCustomerInAdapter_When_Found() {
        Rif rif = new Rif("V-11111111-1");
        Customer customer = createTestCustomer(rif, true, true);

        when(customerRepository.findActivePortalCustomerByRif(rif)).thenReturn(Optional.of(customer));

        Optional<Authenticable> result = resolver.resolveByRif(rif);

        assertTrue(result.isPresent());
        assertInstanceOf(CustomerAuthenticableAdapter.class, result.get());
        assertSame(customer, ((CustomerAuthenticableAdapter) result.get()).getCustomer());
    }

    private Customer createTestCustomer(Rif rif, boolean active, boolean portalAccess) {
        return Customer.reconstitute(
            CustomerId.generate(),
            new CustomerCode(2001L),
            new LegalName("Resolver Test Customer"),
            CustomerType.BUSINESS,
            rif,
            new CustomerAlias("ResolverTest"),
            new CreditDays(30),
            new CreditLimit(BigDecimal.valueOf(5000)),
            active,
            LocalDateTime.now(),
            LocalDateTime.now(),
            List.of(),
            List.of(),
            List.of(),
            portalAccess,
            "$2a$10$testHashedPassword",
            null,
            null
        );
    }
}
