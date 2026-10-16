package com.cashly.cashly_api.customers.profile.domain.valueobjects;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CustomerIdUnitTest {

    @Test
    void should_generate_unique_ids() {
        CustomerId id1 = CustomerId.generate();
        CustomerId id2 = CustomerId.generate();
        assertNotNull(id1);
        assertNotNull(id2);
        assertNotEquals(id1, id2);
    }

    @Test
    void should_parse_valid_uuid_string() {
        String uuid = "550e8400-e29b-41d4-a716-446655440000";
        CustomerId id = CustomerId.from(uuid);
        assertEquals(UUID.fromString(uuid), id.getValue());
    }

    @Test
    void should_throw_on_null_string() {
        assertThrows(IllegalArgumentException.class, () -> CustomerId.from(null));
    }

    @Test
    void should_throw_on_empty_string() {
        assertThrows(IllegalArgumentException.class, () -> CustomerId.from(""));
    }

    @Test
    void should_throw_on_blank_string() {
        assertThrows(IllegalArgumentException.class, () -> CustomerId.from("   "));
    }

    @Test
    void should_throw_on_invalid_uuid_format() {
        assertThrows(IllegalArgumentException.class, () -> CustomerId.from("not-a-uuid"));
    }

    @Test
    void should_create_with_valid_uuid() {
        UUID uuid = UUID.randomUUID();
        CustomerId id = new CustomerId(uuid);
        assertEquals(uuid, id.getValue());
    }

    @Test
    void should_throw_on_null_uuid() {
        assertThrows(IllegalArgumentException.class, () -> new CustomerId(null));
    }

    @Test
    void should_be_equal_for_same_value() {
        UUID uuid = UUID.randomUUID();
        CustomerId id1 = new CustomerId(uuid);
        CustomerId id2 = new CustomerId(uuid);
        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    void should_not_be_equal_for_different_values() {
        CustomerId id1 = CustomerId.generate();
        CustomerId id2 = CustomerId.generate();
        assertNotEquals(id1, id2);
    }

    @Test
    void should_return_value_in_toString() {
        UUID uuid = UUID.randomUUID();
        CustomerId id = new CustomerId(uuid);
        assertEquals(uuid.toString(), id.toString());
    }
}
