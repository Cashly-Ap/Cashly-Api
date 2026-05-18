package com.parsun.parsun_api.customers.domain.entities;

import com.parsun.parsun_api.customers.domain.valueobjects.CustomerId;
import com.parsun.parsun_api.customers.domain.valueobjects.EmailAddress;
import com.parsun.parsun_api.customers.domain.valueobjects.EmailId;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomerEmailUnitTest {

    @Test
    void should_create_with_generated_id() {
        CustomerId customerId = CustomerId.generate();
        EmailAddress email = new EmailAddress("test@example.com");

        CustomerEmail customerEmail = CustomerEmail.create(customerId, email);

        assertNotNull(customerEmail.getId());
        assertEquals(customerId, customerEmail.getCustomerId());
        assertEquals(email, customerEmail.getEmail());
    }

    @Test
    void should_throw_on_null_id() {
        CustomerId customerId = CustomerId.generate();
        EmailAddress email = new EmailAddress("test@example.com");

        assertThrows(IllegalArgumentException.class, () ->
                new CustomerEmail(null, customerId, email));
    }

    @Test
    void should_throw_on_null_customer_id() {
        EmailId id = EmailId.generate();
        EmailAddress email = new EmailAddress("test@example.com");

        assertThrows(IllegalArgumentException.class, () ->
                new CustomerEmail(id, null, email));
    }

    @Test
    void should_throw_on_null_email() {
        EmailId id = EmailId.generate();
        CustomerId customerId = CustomerId.generate();

        assertThrows(IllegalArgumentException.class, () ->
                new CustomerEmail(id, customerId, null));
    }

    @Test
    void should_be_equal_by_id_only() {
        EmailId id = EmailId.generate();
        CustomerId customerId = CustomerId.generate();

        CustomerEmail email1 = new CustomerEmail(id, customerId, new EmailAddress("test@example.com"));
        CustomerEmail email2 = new CustomerEmail(id, customerId, new EmailAddress("other@example.com"));

        assertEquals(email1, email2);
        assertEquals(email1.hashCode(), email2.hashCode());
    }

    @Test
    void should_not_be_equal_for_different_ids() {
        CustomerId customerId = CustomerId.generate();
        EmailAddress email = new EmailAddress("test@example.com");

        CustomerEmail email1 = CustomerEmail.create(customerId, email);
        CustomerEmail email2 = CustomerEmail.create(customerId, email);

        assertNotEquals(email1, email2);
    }
}
