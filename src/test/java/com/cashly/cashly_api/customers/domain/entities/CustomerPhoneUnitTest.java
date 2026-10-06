package com.cashly.cashly_api.customers.domain.entities;

import com.cashly.cashly_api.customers.domain.valueobjects.CustomerId;
import com.cashly.cashly_api.customers.domain.valueobjects.PhoneId;
import com.cashly.cashly_api.customers.domain.valueobjects.PhoneNumber;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomerPhoneUnitTest {

    @Test
    void should_create_with_generated_id() {
        CustomerId customerId = CustomerId.generate();
        PhoneNumber phone = new PhoneNumber("+58-212-555-1234");

        CustomerPhone customerPhone = CustomerPhone.create(customerId, phone);

        assertNotNull(customerPhone.getId());
        assertEquals(customerId, customerPhone.getCustomerId());
        assertEquals(phone, customerPhone.getPhone());
    }

    @Test
    void should_throw_on_null_id() {
        CustomerId customerId = CustomerId.generate();
        PhoneNumber phone = new PhoneNumber("+58-212-555-1234");

        assertThrows(IllegalArgumentException.class, () ->
                new CustomerPhone(null, customerId, phone));
    }

    @Test
    void should_throw_on_null_customer_id() {
        PhoneId id = PhoneId.generate();
        PhoneNumber phone = new PhoneNumber("+58-212-555-1234");

        assertThrows(IllegalArgumentException.class, () ->
                new CustomerPhone(id, null, phone));
    }

    @Test
    void should_throw_on_null_phone() {
        PhoneId id = PhoneId.generate();
        CustomerId customerId = CustomerId.generate();

        assertThrows(IllegalArgumentException.class, () ->
                new CustomerPhone(id, customerId, null));
    }

    @Test
    void should_be_equal_by_id_only() {
        PhoneId id = PhoneId.generate();
        CustomerId customerId = CustomerId.generate();

        CustomerPhone phone1 = new CustomerPhone(id, customerId, new PhoneNumber("5551234"));
        CustomerPhone phone2 = new CustomerPhone(id, customerId, new PhoneNumber("5555678"));

        assertEquals(phone1, phone2);
        assertEquals(phone1.hashCode(), phone2.hashCode());
    }

    @Test
    void should_not_be_equal_for_different_ids() {
        CustomerId customerId = CustomerId.generate();
        PhoneNumber phone = new PhoneNumber("+58-212-555-1234");

        CustomerPhone phone1 = CustomerPhone.create(customerId, phone);
        CustomerPhone phone2 = CustomerPhone.create(customerId, phone);

        assertNotEquals(phone1, phone2);
    }
}
