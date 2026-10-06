package com.cashly.cashly_api.customers.domain.entities;

import com.cashly.cashly_api.customers.domain.valueobjects.*;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CustomerUnitTest {

    @Test
    void should_create_active_customer_with_empty_collections() {
        Customer customer = Customer.create(
                new CustomerCode(1L),
                new LegalName("Cashly C.A."),
                CustomerType.BUSINESS,
                new Rif("J-12345678-9"),
                new CustomerAlias("Cashly"),
                new CreditDays(30),
                new CreditLimit(new BigDecimal("1000.00"))
        );

        assertNotNull(customer.getId());
        assertTrue(customer.isActive());
        assertNotNull(customer.getCreatedAt());
        assertNotNull(customer.getUpdatedAt());
        assertTrue(customer.getEmails().isEmpty());
        assertTrue(customer.getPhones().isEmpty());
        assertTrue(customer.getAddresses().isEmpty());
        assertFalse(customer.isPortalAccess());
        assertNull(customer.getPortalPassword());
    }

    @Test
    void should_reconstitute_customer_with_state() {
        CustomerId id = CustomerId.generate();
        LocalDateTime now = LocalDateTime.now();
        List<CustomerEmail> emails = List.of(
                new CustomerEmail(EmailId.generate(), id, new EmailAddress("test@example.com"))
        );

        Customer customer = Customer.reconstitute(
                id,
                new CustomerCode(1L),
                new LegalName("Cashly C.A."),
                CustomerType.BUSINESS,
                new Rif("J-12345678-9"),
                new CustomerAlias("Cashly"),
                new CreditDays(30),
                new CreditLimit(new BigDecimal("1000.00")),
                false,
                now,
                now,
                emails,
                List.of(),
                List.of(),
                true,
                "hashed-password",
                "Some notes",
                UUID.randomUUID()
        );

        assertEquals(id, customer.getId());
        assertEquals(1L, customer.getCode().getValue());
        assertEquals("Cashly C.A.", customer.getLegalName().getValue());
        assertEquals(CustomerType.BUSINESS, customer.getType());
        assertEquals("J-12345678-9", customer.getRif().getValue());
        assertEquals("Cashly", customer.getAlias().getValue());
        assertEquals(30, customer.getCreditDays().getValue());
        assertEquals(new BigDecimal("1000.00"), customer.getCreditLimit().getValue());
        assertFalse(customer.isActive());
        assertEquals(now, customer.getCreatedAt());
        assertEquals(now, customer.getUpdatedAt());
        assertEquals(1, customer.getEmails().size());
        assertTrue(customer.isPortalAccess());
        assertEquals("hashed-password", customer.getPortalPassword());
        assertEquals("Some notes", customer.getNotes());
        assertNotNull(customer.getPriceTypeId());
    }

    @Test
    void should_activate_customer() {
        Customer customer = createTestCustomer();
        customer.deactivate();
        assertFalse(customer.isActive());

        customer.activate();
        assertTrue(customer.isActive());
        assertNotNull(customer.getUpdatedAt());
    }

    @Test
    void should_deactivate_customer() {
        Customer customer = createTestCustomer();
        assertTrue(customer.isActive());

        customer.deactivate();
        assertFalse(customer.isActive());
        assertNotNull(customer.getUpdatedAt());
    }

    @Test
    void should_add_email_to_collection() {
        Customer customer = createTestCustomer();
        assertTrue(customer.getEmails().isEmpty());

        customer.addEmail(new EmailAddress("test@example.com"));
        assertEquals(1, customer.getEmails().size());
        assertEquals("test@example.com", customer.getEmails().get(0).getEmail().getValue());
    }

    @Test
    void should_add_phone_to_collection() {
        Customer customer = createTestCustomer();
        assertTrue(customer.getPhones().isEmpty());

        customer.addPhone(new PhoneNumber("+58-212-555-1234"));
        assertEquals(1, customer.getPhones().size());
        assertEquals("+58-212-555-1234", customer.getPhones().get(0).getPhone().getValue());
    }

    @Test
    void should_add_address_to_collection() {
        Customer customer = createTestCustomer();
        assertTrue(customer.getAddresses().isEmpty());

        customer.addAddress("123 Main St", true, true, "State", "Municipality", "1000");
        assertEquals(1, customer.getAddresses().size());
        assertEquals("123 Main St", customer.getAddresses().get(0).getAddress());
        assertTrue(customer.getAddresses().get(0).isBilling());
    }

    @Test
    void should_clear_other_billing_addresses_when_adding_billing() {
        Customer customer = createTestCustomer();
        customer.addAddress("Address 1", true, true, "State", "Mun", "1000");
        customer.addAddress("Address 2", true, true, "State", "Mun", "1000");

        
        assertFalse(customer.getAddresses().get(0).isBilling());
        
        assertTrue(customer.getAddresses().get(1).isBilling());
    }

    @Test
    void should_clear_emails() {
        Customer customer = createTestCustomer();
        customer.addEmail(new EmailAddress("test@example.com"));
        assertEquals(1, customer.getEmails().size());

        customer.clearEmails();
        assertTrue(customer.getEmails().isEmpty());
    }

    @Test
    void should_clear_phones() {
        Customer customer = createTestCustomer();
        customer.addPhone(new PhoneNumber("5551234"));
        assertEquals(1, customer.getPhones().size());

        customer.clearPhones();
        assertTrue(customer.getPhones().isEmpty());
    }

    @Test
    void should_clear_addresses() {
        Customer customer = createTestCustomer();
        customer.addAddress("123 Main St", false, true, "State", "Mun", "1000");
        assertEquals(1, customer.getAddresses().size());

        customer.clearAddresses();
        assertTrue(customer.getAddresses().isEmpty());
    }

    @Test
    void should_enable_portal_access() {
        Customer customer = createTestCustomer();
        assertFalse(customer.isPortalAccess());

        customer.enablePortalAccess("hashed-password");
        assertTrue(customer.isPortalAccess());
        assertEquals("hashed-password", customer.getPortalPassword());
    }

    @Test
    void should_disable_portal_access() {
        Customer customer = createTestCustomer();
        customer.enablePortalAccess("hashed-password");
        assertTrue(customer.isPortalAccess());

        customer.disablePortalAccess();
        assertFalse(customer.isPortalAccess());
    }

    @Test
    void should_change_portal_password() {
        Customer customer = createTestCustomer();
        customer.enablePortalAccess("old-password");
        assertEquals("old-password", customer.getPortalPassword());

        customer.changePortalPassword("new-password");
        assertEquals("new-password", customer.getPortalPassword());
    }

    @Test
    void should_return_credit_when_credit_limit_positive() {
        Customer customer = Customer.create(
                new CustomerCode(1L),
                new LegalName("Cashly C.A."),
                CustomerType.BUSINESS,
                new Rif("J-12345678-9"),
                new CustomerAlias("Cashly"),
                new CreditDays(30),
                new CreditLimit(new BigDecimal("1000.00"))
        );
        assertEquals(PaymentType.CREDIT, customer.getPaymentType());
    }

    @Test
    void should_return_cash_when_credit_limit_zero() {
        Customer customer = Customer.create(
                new CustomerCode(1L),
                new LegalName("Cashly C.A."),
                CustomerType.BUSINESS,
                new Rif("J-12345678-9"),
                new CustomerAlias("Cashly"),
                new CreditDays(0),
                new CreditLimit(BigDecimal.ZERO)
        );
        assertEquals(PaymentType.CASH, customer.getPaymentType());
    }

    @Test
    void should_update_legal_name() {
        Customer customer = createTestCustomer();
        LocalDateTime before = customer.getUpdatedAt();

        customer.updateLegalName(new LegalName("New Name C.A."));
        assertEquals("New Name C.A.", customer.getLegalName().getValue());
        assertTrue(customer.getUpdatedAt().isAfter(before) || customer.getUpdatedAt().equals(before));
    }

    @Test
    void should_update_rif() {
        Customer customer = createTestCustomer();
        LocalDateTime before = customer.getUpdatedAt();

        customer.updateRif(new Rif("V-87654321-1"));
        assertEquals("V-87654321-1", customer.getRif().getValue());
        assertTrue(customer.getUpdatedAt().isAfter(before) || customer.getUpdatedAt().equals(before));
    }

    @Test
    void should_update_alias() {
        Customer customer = createTestCustomer();
        LocalDateTime before = customer.getUpdatedAt();

        customer.updateAlias(new CustomerAlias("NewAlias"));
        assertEquals("NewAlias", customer.getAlias().getValue());
        assertTrue(customer.getUpdatedAt().isAfter(before) || customer.getUpdatedAt().equals(before));
    }

    @Test
    void should_update_credit_days() {
        Customer customer = createTestCustomer();
        LocalDateTime before = customer.getUpdatedAt();

        customer.updateCreditDays(new CreditDays(60));
        assertEquals(60, customer.getCreditDays().getValue());
        assertTrue(customer.getUpdatedAt().isAfter(before) || customer.getUpdatedAt().equals(before));
    }

    @Test
    void should_update_credit_limit() {
        Customer customer = createTestCustomer();
        LocalDateTime before = customer.getUpdatedAt();

        customer.updateCreditLimit(new CreditLimit(new BigDecimal("5000.00")));
        assertEquals(new BigDecimal("5000.00"), customer.getCreditLimit().getValue());
        assertTrue(customer.getUpdatedAt().isAfter(before) || customer.getUpdatedAt().equals(before));
    }

    @Test
    void should_update_type() {
        Customer customer = createTestCustomer();
        LocalDateTime before = customer.getUpdatedAt();

        customer.updateType(CustomerType.INDIVIDUAL);
        assertEquals(CustomerType.INDIVIDUAL, customer.getType());
        assertTrue(customer.getUpdatedAt().isAfter(before) || customer.getUpdatedAt().equals(before));
    }

    @Test
    void should_update_notes() {
        Customer customer = createTestCustomer();
        LocalDateTime before = customer.getUpdatedAt();

        customer.updateNotes("Important notes");
        assertEquals("Important notes", customer.getNotes());
        assertTrue(customer.getUpdatedAt().isAfter(before) || customer.getUpdatedAt().equals(before));
    }

    @Test
    void should_update_price_type_id() {
        Customer customer = createTestCustomer();
        LocalDateTime before = customer.getUpdatedAt();
        UUID newPriceTypeId = UUID.randomUUID();

        customer.updatePriceTypeId(newPriceTypeId);
        assertEquals(newPriceTypeId, customer.getPriceTypeId());
        assertTrue(customer.getUpdatedAt().isAfter(before) || customer.getUpdatedAt().equals(before));
    }

    @Test
    void should_be_equal_by_id_only() {
        CustomerId id = CustomerId.generate();
        LocalDateTime now = LocalDateTime.now();

        Customer customer1 = Customer.reconstitute(
                id, new CustomerCode(1L), new LegalName("Name 1"), CustomerType.BUSINESS,
                new Rif("J-11111111-1"), new CustomerAlias("Alias1"),
                new CreditDays(30), new CreditLimit(new BigDecimal("1000")),
                true, now, now, List.of(), List.of(), List.of(),
                false, null, null, null
        );

        Customer customer2 = Customer.reconstitute(
                id, new CustomerCode(2L), new LegalName("Name 2"), CustomerType.INDIVIDUAL,
                new Rif("J-22222222-2"), new CustomerAlias("Alias2"),
                new CreditDays(60), new CreditLimit(new BigDecimal("2000")),
                false, now, now, List.of(), List.of(), List.of(),
                false, null, null, null
        );

        assertEquals(customer1, customer2);
        assertEquals(customer1.hashCode(), customer2.hashCode());
    }

    @Test
    void should_not_be_equal_for_different_ids() {
        Customer customer1 = createTestCustomer();
        Customer customer2 = createTestCustomer();
        assertNotEquals(customer1, customer2);
    }

    @Test
    void should_return_unmodifiable_emails() {
        Customer customer = createTestCustomer();
        assertThrows(UnsupportedOperationException.class, () -> customer.getEmails().add(null));
    }

    @Test
    void should_return_unmodifiable_phones() {
        Customer customer = createTestCustomer();
        assertThrows(UnsupportedOperationException.class, () -> customer.getPhones().add(null));
    }

    @Test
    void should_return_unmodifiable_addresses() {
        Customer customer = createTestCustomer();
        assertThrows(UnsupportedOperationException.class, () -> customer.getAddresses().add(null));
    }

    private Customer createTestCustomer() {
        return Customer.create(
                new CustomerCode(1L),
                new LegalName("Cashly C.A."),
                CustomerType.BUSINESS,
                new Rif("J-12345678-9"),
                new CustomerAlias("Cashly"),
                new CreditDays(30),
                new CreditLimit(new BigDecimal("1000.00"))
        );
    }
}
