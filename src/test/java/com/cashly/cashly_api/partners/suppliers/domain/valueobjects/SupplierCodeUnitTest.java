package com.cashly.cashly_api.partners.suppliers.domain.valueobjects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SupplierCodeUnitTest {

    @Test
    void should_create_valid_positive_code() {
        SupplierCode code = new SupplierCode(1L);
        assertEquals(1L, code.getValue());
    }

    @Test
    void should_create_valid_large_code() {
        SupplierCode code = new SupplierCode(999999L);
        assertEquals(999999L, code.getValue());
    }

    @Test
    void should_create_from_primitive_long() {
        SupplierCode code = SupplierCode.from(100L);
        assertEquals(100L, code.getValue());
    }

    @Test
    void should_throw_on_null() {
        assertThrows(IllegalArgumentException.class,
                () -> new SupplierCode(null));
    }

    @Test
    void should_throw_on_zero() {
        assertThrows(IllegalArgumentException.class,
                () -> new SupplierCode(0L));
    }

    @Test
    void should_throw_on_negative() {
        assertThrows(IllegalArgumentException.class,
                () -> new SupplierCode(-1L));
    }

    @Test
    void should_be_equal_for_same_value() {
        SupplierCode code1 = new SupplierCode(123L);
        SupplierCode code2 = new SupplierCode(123L);
        assertEquals(code1, code2);
        assertEquals(code1.hashCode(), code2.hashCode());
    }

    @Test
    void should_not_be_equal_for_different_values() {
        SupplierCode code1 = new SupplierCode(123L);
        SupplierCode code2 = new SupplierCode(456L);
        assertNotEquals(code1, code2);
    }

    @Test
    void should_return_value_in_toString() {
        SupplierCode code = new SupplierCode(123L);
        assertEquals("123", code.toString());
    }
}
