package com.parsun.parsun_api.customers.domain.valueobjects;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EmailIdUnitTest {

    @Test
    void should_generate_unique_ids() {
        EmailId id1 = EmailId.generate();
        EmailId id2 = EmailId.generate();
        assertNotNull(id1);
        assertNotNull(id2);
        assertNotEquals(id1, id2);
    }

    @Test
    void should_create_with_valid_uuid() {
        UUID uuid = UUID.randomUUID();
        EmailId id = new EmailId(uuid);
        assertEquals(uuid, id.getValue());
    }

    @Test
    void should_throw_on_null_uuid() {
        assertThrows(IllegalArgumentException.class, () -> new EmailId(null));
    }

    @Test
    void should_be_equal_for_same_value() {
        UUID uuid = UUID.randomUUID();
        EmailId id1 = new EmailId(uuid);
        EmailId id2 = new EmailId(uuid);
        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    void should_not_be_equal_for_different_values() {
        EmailId id1 = EmailId.generate();
        EmailId id2 = EmailId.generate();
        assertNotEquals(id1, id2);
    }

    @Test
    void should_return_value_in_toString() {
        UUID uuid = UUID.randomUUID();
        EmailId id = new EmailId(uuid);
        assertEquals(uuid.toString(), id.toString());
    }
}
