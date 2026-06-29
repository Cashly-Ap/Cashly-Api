package com.parsun.parsun_api.suppliers.domain.valueobjects;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SupplierIdUnitTest {

    @Test
    void should_generate_unique_ids() {
        SupplierId id1 = SupplierId.generate();
        SupplierId id2 = SupplierId.generate();
        assertNotNull(id1);
        assertNotNull(id2);
        assertNotEquals(id1, id2);
    }

    @Test
    void should_parse_valid_uuid_string() {
        String uuid = "550e8400-e29b-41d4-a716-446655440000";
        SupplierId id = SupplierId.from(uuid);
        assertEquals(UUID.fromString(uuid), id.getValue());
    }

    @Test
    void should_throw_on_null_string() {
        assertThrows(IllegalArgumentException.class, () -> SupplierId.from(null));
    }

    @Test
    void should_throw_on_empty_string() {
        assertThrows(IllegalArgumentException.class, () -> SupplierId.from(""));
    }

    @Test
    void should_throw_on_blank_string() {
        assertThrows(IllegalArgumentException.class, () -> SupplierId.from("   "));
    }

    @Test
    void should_throw_on_invalid_uuid_format() {
        assertThrows(IllegalArgumentException.class, () -> SupplierId.from("not-a-uuid"));
    }

    @Test
    void should_create_with_valid_uuid() {
        UUID uuid = UUID.randomUUID();
        SupplierId id = new SupplierId(uuid);
        assertEquals(uuid, id.getValue());
    }

    @Test
    void should_throw_on_null_uuid() {
        assertThrows(IllegalArgumentException.class, () -> new SupplierId(null));
    }

    @Test
    void should_be_equal_for_same_value() {
        UUID uuid = UUID.randomUUID();
        SupplierId id1 = new SupplierId(uuid);
        SupplierId id2 = new SupplierId(uuid);
        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    void should_not_be_equal_for_different_values() {
        SupplierId id1 = SupplierId.generate();
        SupplierId id2 = SupplierId.generate();
        assertNotEquals(id1, id2);
    }

    @Test
    void should_return_value_in_toString() {
        UUID uuid = UUID.randomUUID();
        SupplierId id = new SupplierId(uuid);
        assertEquals(uuid.toString(), id.toString());
    }
}
