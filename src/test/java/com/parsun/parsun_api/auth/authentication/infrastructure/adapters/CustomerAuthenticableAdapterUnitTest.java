package com.parsun.parsun_api.auth.authentication.infrastructure.adapters;

import com.parsun.parsun_api.auth.authentication.domain.entities.Authenticable;
import com.parsun.parsun_api.auth.authentication.domain.entities.AuthenticableType;
import com.parsun.parsun_api.customers.domain.entities.Customer;
import com.parsun.parsun_api.customers.domain.entities.CustomerEmail;
import com.parsun.parsun_api.customers.domain.valueobjects.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CustomerAuthenticableAdapter Unit Tests")
class CustomerAuthenticableAdapterUnitTest {

    private Customer customer;
    private CustomerId customerId;
    private CustomerAuthenticableAdapter adapter;

    @BeforeEach
    void setUp() {
        customerId = CustomerId.generate();
        Rif rif = new Rif("J-12345678-9");
        EmailAddress emailAddress = new EmailAddress("customer@example.com");
        CustomerEmail customerEmail = CustomerEmail.create(customerId, emailAddress);

        customer = Customer.reconstitute(
            customerId,
            new CustomerCode(1001L),
            new LegalName("Test Customer S.A."),
            CustomerType.BUSINESS,
            rif,
            new CustomerAlias("TestAlias"),
            new CreditDays(30),
            new CreditLimit(BigDecimal.valueOf(1000)),
            true,
            LocalDateTime.now(),
            LocalDateTime.now(),
            List.of(customerEmail),
            List.of(),
            List.of(),
            true,
            "$2a$10$hashedPortalPassword",
            "Some notes",
            null
        );

        adapter = new CustomerAuthenticableAdapter(customer);
    }

    @Test
    @DisplayName("should_ReturnSubjectId_When_GetSubjectIdCalled")
    void should_ReturnSubjectId_When_GetSubjectIdCalled() {
        assertEquals(customerId.getValue().toString(), adapter.getSubjectId());
    }

    @Test
    @DisplayName("should_ReturnFirstEmail_When_GetEmailCalled")
    void should_ReturnFirstEmail_When_GetEmailCalled() {
        assertEquals("customer@example.com", adapter.getEmail());
    }

    @Test
    @DisplayName("should_ReturnEmptyEmail_When_CustomerHasNoEmails")
    void should_ReturnEmptyEmail_When_CustomerHasNoEmails() {
        Customer customerNoEmail = Customer.reconstitute(
            CustomerId.generate(), new CustomerCode(1002L), new LegalName("No Email Customer"),
            CustomerType.INDIVIDUAL, new Rif("V-87654321-0"), new CustomerAlias("NoEmail"),
            new CreditDays(15), new CreditLimit(BigDecimal.ZERO),
            true, LocalDateTime.now(), LocalDateTime.now(),
            List.of(), List.of(), List.of(),
            true, "$2a$10$hashed", null, null
        );
        CustomerAuthenticableAdapter adapterNoEmail = new CustomerAuthenticableAdapter(customerNoEmail);

        assertEquals("", adapterNoEmail.getEmail());
    }

    @Test
    @DisplayName("should_ReturnHashedPassword_When_GetHashedPasswordCalled")
    void should_ReturnHashedPassword_When_GetHashedPasswordCalled() {
        assertEquals("$2a$10$hashedPortalPassword", adapter.getHashedPassword());
    }

    @Test
    @DisplayName("should_ReturnLegalName_When_GetUserNameCalled")
    void should_ReturnLegalName_When_GetUserNameCalled() {
        assertEquals("Test Customer S.A.", adapter.getUserName());
    }

    @Test
    @DisplayName("should_ReturnCUSTOMERType_When_GetTypeCalled")
    void should_ReturnCUSTOMERType_When_GetTypeCalled() {
        assertEquals(AuthenticableType.CUSTOMER, adapter.getType());
    }

    @Test
    @DisplayName("should_ReturnTrue_When_CanAuthenticateAndActiveWithPortalAccess")
    void should_ReturnTrue_When_CanAuthenticateAndActiveWithPortalAccess() {
        assertTrue(adapter.canAuthenticate());
    }

    @Test
    @DisplayName("should_ReturnFalse_When_CanAuthenticateAndInactive")
    void should_ReturnFalse_When_CanAuthenticateAndInactive() {
        Customer inactiveCustomer = Customer.reconstitute(
            CustomerId.generate(), new CustomerCode(1003L), new LegalName("Inactive Customer"),
            CustomerType.BUSINESS, new Rif("J-11111111-1"), new CustomerAlias("Inactive"),
            new CreditDays(30), new CreditLimit(BigDecimal.valueOf(1000)),
            false, LocalDateTime.now(), LocalDateTime.now(),
            List.of(), List.of(), List.of(),
            true, "$2a$10$hashed", null, null
        );
        CustomerAuthenticableAdapter inactiveAdapter = new CustomerAuthenticableAdapter(inactiveCustomer);

        assertFalse(inactiveAdapter.canAuthenticate());
    }

    @Test
    @DisplayName("should_ReturnFalse_When_CanAuthenticateAndPortalAccessDisabled")
    void should_ReturnFalse_When_CanAuthenticateAndPortalAccessDisabled() {
        Customer noPortalCustomer = Customer.reconstitute(
            CustomerId.generate(), new CustomerCode(1004L), new LegalName("No Portal Customer"),
            CustomerType.BUSINESS, new Rif("J-22222222-2"), new CustomerAlias("NoPortal"),
            new CreditDays(30), new CreditLimit(BigDecimal.valueOf(1000)),
            true, LocalDateTime.now(), LocalDateTime.now(),
            List.of(), List.of(), List.of(),
            false, "$2a$10$hashed", null, null
        );
        CustomerAuthenticableAdapter noPortalAdapter = new CustomerAuthenticableAdapter(noPortalCustomer);

        assertFalse(noPortalAdapter.canAuthenticate());
    }

    @Test
    @DisplayName("should_ReturnIsActive_When_IsActiveCalled")
    void should_ReturnIsActive_When_IsActiveCalled() {
        assertTrue(adapter.isActive());
    }

    @Test
    @DisplayName("should_ReturnCreatedAt_When_GetCreatedAtCalled")
    void should_ReturnCreatedAt_When_GetCreatedAtCalled() {
        assertNotNull(adapter.getCreatedAt());
    }

    @Test
    @DisplayName("should_ReturnNullUpdatedAt_When_GetUpdatedAtCalled")
    void should_ReturnNullUpdatedAt_When_GetUpdatedAtCalled() {
        assertNull(adapter.getUpdatedAt());
    }

    @Test
    @DisplayName("should_ReturnNullRole_When_GetRoleCalled")
    void should_ReturnNullRole_When_GetRoleCalled() {
        assertNull(adapter.getRole());
    }

    @Test
    @DisplayName("should_ReturnEmptyModulePermissions_When_GetModulePermissionsCalled")
    void should_ReturnEmptyModulePermissions_When_GetModulePermissionsCalled() {
        assertTrue(adapter.getModulePermissions().isEmpty());
    }

    @Test
    @DisplayName("should_ReturnCustomer_When_GetCustomerCalled")
    void should_ReturnCustomer_When_GetCustomerCalled() {
        assertSame(customer, adapter.getCustomer());
    }

    @Test
    @DisplayName("should_ImplementAuthenticableInterface")
    void should_ImplementAuthenticableInterface() {
        assertTrue(adapter instanceof Authenticable);
    }
}
