package com.cashly.cashly_api.customers.profile.domain;

import com.cashly.cashly_api.customers.profile.domain.valueobjects.AddressId;
import com.cashly.cashly_api.customers.profile.domain.valueobjects.CustomerId;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomerAddressUnitTest {

    @Test
    void should_create_with_generated_id() {
        CustomerId customerId = CustomerId.generate();
        CustomerAddress address = CustomerAddress.create(customerId, "123 Main St", false, true, "State", "Municipality", "1000");

        assertNotNull(address.getId());
        assertEquals(customerId, address.getCustomerId());
        assertEquals("123 Main St", address.getAddress());
        assertFalse(address.isBilling());
        assertTrue(address.isActive());
        assertEquals("State", address.getState());
        assertEquals("Municipality", address.getMunicipality());
        assertEquals("1000", address.getPostalCode());
    }

    @Test
    void should_throw_on_null_id() {
        CustomerId customerId = CustomerId.generate();
        assertThrows(IllegalArgumentException.class, () ->
                new CustomerAddress(null, customerId, "123 Main St", false, true, "State", "Mun", "1000"));
    }

    @Test
    void should_throw_on_null_customer_id() {
        assertThrows(IllegalArgumentException.class, () ->
                new CustomerAddress(AddressId.generate(), null, "123 Main St", false, true, "State", "Mun", "1000"));
    }

    @Test
    void should_throw_on_null_address() {
        CustomerId customerId = CustomerId.generate();
        assertThrows(IllegalArgumentException.class, () ->
                new CustomerAddress(AddressId.generate(), customerId, null, false, true, "State", "Mun", "1000"));
    }

    @Test
    void should_throw_on_blank_address() {
        CustomerId customerId = CustomerId.generate();
        assertThrows(IllegalArgumentException.class, () ->
                new CustomerAddress(AddressId.generate(), customerId, "   ", false, true, "State", "Mun", "1000"));
    }

    @Test
    void should_mark_as_billing() {
        CustomerId customerId = CustomerId.generate();
        CustomerAddress address = CustomerAddress.create(customerId, "123 Main St", false, true, "State", "Mun", "1000");
        assertFalse(address.isBilling());

        address.markAsBilling();
        assertTrue(address.isBilling());
    }

    @Test
    void should_unmark_as_billing() {
        CustomerId customerId = CustomerId.generate();
        CustomerAddress address = CustomerAddress.create(customerId, "123 Main St", true, true, "State", "Mun", "1000");
        assertTrue(address.isBilling());

        address.unmarkAsBilling();
        assertFalse(address.isBilling());
    }

    @Test
    void should_activate() {
        CustomerId customerId = CustomerId.generate();
        CustomerAddress address = CustomerAddress.create(customerId, "123 Main St", false, false, "State", "Mun", "1000");
        assertFalse(address.isActive());

        address.activate();
        assertTrue(address.isActive());
    }

    @Test
    void should_deactivate() {
        CustomerId customerId = CustomerId.generate();
        CustomerAddress address = CustomerAddress.create(customerId, "123 Main St", false, true, "State", "Mun", "1000");
        assertTrue(address.isActive());

        address.deactivate();
        assertFalse(address.isActive());
    }

    @Test
    void should_be_equal_by_id_only() {
        AddressId id = AddressId.generate();
        CustomerId customerId = CustomerId.generate();

        CustomerAddress address1 = new CustomerAddress(id, customerId, "Address 1", false, true, "State1", "Mun1", "1000");
        CustomerAddress address2 = new CustomerAddress(id, customerId, "Address 2", true, false, "State2", "Mun2", "2000");

        assertEquals(address1, address2);
        assertEquals(address1.hashCode(), address2.hashCode());
    }

    @Test
    void should_not_be_equal_for_different_ids() {
        CustomerId customerId = CustomerId.generate();
        CustomerAddress address1 = CustomerAddress.create(customerId, "Address 1", false, true, "State", "Mun", "1000");
        CustomerAddress address2 = CustomerAddress.create(customerId, "Address 2", false, true, "State", "Mun", "1000");
        assertNotEquals(address1, address2);
    }
}
