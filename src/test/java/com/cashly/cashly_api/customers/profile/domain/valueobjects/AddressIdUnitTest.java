package com.cashly.cashly_api.customers.profile.domain.valueobjects;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AddressIdUnitTest {

    @Test
    void should_generate_unique_ids() {
        AddressId id1 = AddressId.generate();
        AddressId id2 = AddressId.generate();
        assertNotNull(id1);
        assertNotNull(id2);
        assertNotEquals(id1, id2);
    }

    @Test
    void should_create_with_valid_uuid() {
        UUID uuid = UUID.randomUUID();
        AddressId id = new AddressId(uuid);
        assertEquals(uuid, id.getValue());
    }

    @Test
    void should_throw_on_null_uuid() {
        assertThrows(IllegalArgumentException.class, () -> new AddressId(null));
    }

    @Test
    void should_be_equal_for_same_value() {
        UUID uuid = UUID.randomUUID();
        AddressId id1 = new AddressId(uuid);
        AddressId id2 = new AddressId(uuid);
        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    void should_not_be_equal_for_different_values() {
        AddressId id1 = AddressId.generate();
        AddressId id2 = AddressId.generate();
        assertNotEquals(id1, id2);
    }

    @Test
    void should_return_value_in_toString() {
        UUID uuid = UUID.randomUUID();
        AddressId id = new AddressId(uuid);
        assertEquals(uuid.toString(), id.toString());
    }
}
