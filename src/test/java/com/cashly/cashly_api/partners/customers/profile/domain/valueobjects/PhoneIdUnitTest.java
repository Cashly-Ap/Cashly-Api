package com.cashly.cashly_api.partners.customers.profile.domain.valueobjects;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PhoneIdUnitTest {

    @Test
    void should_generate_unique_ids() {
        PhoneId id1 = PhoneId.generate();
        PhoneId id2 = PhoneId.generate();
        assertNotNull(id1);
        assertNotNull(id2);
        assertNotEquals(id1, id2);
    }

    @Test
    void should_create_with_valid_uuid() {
        UUID uuid = UUID.randomUUID();
        PhoneId id = new PhoneId(uuid);
        assertEquals(uuid, id.getValue());
    }

    @Test
    void should_throw_on_null_uuid() {
        assertThrows(IllegalArgumentException.class, () -> new PhoneId(null));
    }

    @Test
    void should_be_equal_for_same_value() {
        UUID uuid = UUID.randomUUID();
        PhoneId id1 = new PhoneId(uuid);
        PhoneId id2 = new PhoneId(uuid);
        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    void should_not_be_equal_for_different_values() {
        PhoneId id1 = PhoneId.generate();
        PhoneId id2 = PhoneId.generate();
        assertNotEquals(id1, id2);
    }

    @Test
    void should_return_value_in_toString() {
        UUID uuid = UUID.randomUUID();
        PhoneId id = new PhoneId(uuid);
        assertEquals(uuid.toString(), id.toString());
    }
}
