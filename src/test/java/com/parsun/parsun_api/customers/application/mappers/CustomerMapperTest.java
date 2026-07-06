package com.parsun.parsun_api.customers.application.mappers;

import com.parsun.parsun_api.customers.application.dto.response.CustomerCreateResponse;
import com.parsun.parsun_api.customers.application.dto.response.CustomerDetailResponse;
import com.parsun.parsun_api.customers.application.dto.response.CustomerListResponse;
import com.parsun.parsun_api.customers.domain.entities.Customer;
import com.parsun.parsun_api.customers.domain.entities.CustomerAddress;
import com.parsun.parsun_api.customers.domain.entities.CustomerEmail;
import com.parsun.parsun_api.customers.domain.entities.CustomerPhone;
import com.parsun.parsun_api.customers.domain.valueobjects.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CustomerMapper Unit Tests")
class CustomerMapperTest {

    private final CustomerMapper mapper = new CustomerMapper();

    @Test
    @DisplayName("should map customer to list response")
    void should_map_customer_to_list_response() {
        Customer customer = buildCustomerWithContacts();

        CustomerListResponse response = mapper.toListResponse(customer, false);

        assertNotNull(response);
        assertEquals(customer.getId().toString(), response.id());
        assertEquals(customer.getCode().getValue(), response.code());
        assertEquals(customer.getAlias().getValue(), response.alias());
        assertEquals(customer.getLegalName().getValue(), response.legalName());
        assertEquals(customer.getRif().getValue(), response.rif());
        assertEquals(customer.getType().name(), response.type());
        assertEquals(customer.isActive(), response.active());
        assertEquals(customer.getPaymentType(), response.paymentType());
        assertEquals(customer.isPortalAccess(), response.portalAccess());
    }

    @Test
    @DisplayName("should map customer to detail response")
    void should_map_customer_to_detail_response() {
        Customer customer = buildCustomerWithContacts();

        CustomerDetailResponse response = mapper.toDetailResponse(customer);

        assertNotNull(response);
        assertEquals(customer.getId().toString(), response.id());
        assertEquals(customer.getCode().getValue(), response.code());
        assertEquals(customer.getLegalName().getValue(), response.legalName());
        assertEquals(customer.getType().name(), response.type());
        assertEquals(customer.getRif().getValue(), response.rif());
        assertEquals(customer.getAlias().getValue(), response.alias());
        assertEquals(customer.getCreditDays().getValue(), response.creditDays());
        assertEquals(customer.getCreditLimit().getValue(), response.creditLimit());
        assertEquals(customer.isActive(), response.active());
        assertEquals(customer.getPaymentType(), response.paymentType());
        assertEquals(customer.getCreatedAt(), response.createdAt());
        assertEquals(customer.isPortalAccess(), response.portalAccess());
        assertEquals(customer.getNotes(), response.notes());
        assertEquals(customer.getPriceTypeId(), response.priceTypeId());
    }

    @Test
    @DisplayName("should map customer to create response")
    void should_map_customer_to_create_response() {
        Customer customer = buildCustomerWithContacts();

        CustomerCreateResponse response = mapper.toCreateResponse(customer);

        assertNotNull(response);
        assertEquals(customer.getId().toString(), response.id());
        assertEquals(customer.getCode().getValue(), response.code());
        assertEquals(customer.getLegalName().getValue(), response.legalName());
        assertEquals(customer.getType().name(), response.type());
        assertEquals(customer.getRif().getValue(), response.rif());
        assertEquals(customer.getAlias().getValue(), response.alias());
        assertEquals(customer.getCreditDays().getValue(), response.creditDays());
        assertEquals(customer.getCreditLimit().getValue(), response.creditLimit());
        assertEquals(customer.isActive(), response.active());
        assertEquals(customer.getPaymentType(), response.paymentType());
        assertEquals(customer.getCreatedAt(), response.createdAt());
        assertEquals(customer.isPortalAccess(), response.portalAccess());
        assertEquals(customer.getNotes(), response.notes());
        assertEquals(customer.getPriceTypeId(), response.priceTypeId());
    }

    @Test
    @DisplayName("should map customer with emails phones and addresses")
    void should_map_customer_with_emails_phones_and_addresses() {
        Customer customer = buildCustomerWithContacts();

        CustomerDetailResponse response = mapper.toDetailResponse(customer);

        assertEquals(1, response.emails().size());
        assertEquals("test@example.com", response.emails().getFirst().email());

        assertEquals(1, response.phones().size());
        assertEquals("+58-212-555-1234", response.phones().getFirst().phone());

        assertEquals(1, response.addresses().size());
        assertEquals("123 Main St", response.addresses().getFirst().address());
        assertTrue(response.addresses().getFirst().billing());
        assertTrue(response.addresses().getFirst().active());
        assertEquals("State", response.addresses().getFirst().state());
        assertEquals("Municipality", response.addresses().getFirst().municipality());
        assertEquals("1000", response.addresses().getFirst().postalCode());
    }

    @Test
    @DisplayName("should map customer with empty collections")
    void should_map_customer_with_empty_collections() {
        Customer customer = Customer.reconstitute(
                CustomerId.generate(),
                new CustomerCode(1001L),
                new LegalName("Test Corp"),
                CustomerType.BUSINESS,
                new Rif("J-12345678-9"),
                new CustomerAlias("Test"),
                new CreditDays(30),
                new CreditLimit(BigDecimal.ZERO),
                true,
                LocalDateTime.now(),
                LocalDateTime.now(),
                List.of(), List.of(), List.of(),
                false, null, null, null);

        CustomerDetailResponse response = mapper.toDetailResponse(customer);

        assertTrue(response.emails().isEmpty());
        assertTrue(response.phones().isEmpty());
        assertTrue(response.addresses().isEmpty());
    }

    private Customer buildCustomerWithContacts() {
        UUID customerId = UUID.randomUUID();
        CustomerId id = new CustomerId(customerId);
        LocalDateTime now = LocalDateTime.now();

        List<CustomerEmail> emails = List.of(
                new CustomerEmail(EmailId.generate(), id, new EmailAddress("test@example.com")));
        List<CustomerPhone> phones = List.of(
                new CustomerPhone(PhoneId.generate(), id, new PhoneNumber("+58-212-555-1234")));
        List<CustomerAddress> addresses = List.of(
                new CustomerAddress(AddressId.generate(), id, "123 Main St", true, true,
                        "State", "Municipality", "1000"));

        return Customer.reconstitute(
                id,
                new CustomerCode(1001L),
                new LegalName("Test Corp"),
                CustomerType.BUSINESS,
                new Rif("J-12345678-9"),
                new CustomerAlias("Test"),
                new CreditDays(30),
                new CreditLimit(new BigDecimal("5000.00")),
                true,
                now,
                now,
                emails,
                phones,
                addresses,
                true,
                "hashed-password",
                "Important notes",
                UUID.randomUUID()
        );
    }
}
