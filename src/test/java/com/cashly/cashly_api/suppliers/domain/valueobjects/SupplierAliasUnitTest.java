package com.cashly.cashly_api.suppliers.domain.valueobjects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SupplierAliasUnitTest {

    @Test
    void should_create_valid_alias() {
        SupplierAlias alias = new SupplierAlias("Proveedor");
        assertEquals("Proveedor", alias.getValue());
    }

    @Test
    void should_create_minimum_length_alias() {
        SupplierAlias alias = new SupplierAlias("AB");
        assertEquals("AB", alias.getValue());
    }

    @Test
    void should_create_maximum_length_alias() {
        String maxAlias = "A".repeat(100);
        SupplierAlias alias = new SupplierAlias(maxAlias);
        assertEquals(100, alias.getValue().length());
    }

    @Test
    void should_trim_whitespace() {
        SupplierAlias alias = new SupplierAlias("  Proveedor  ");
        assertEquals("Proveedor", alias.getValue());
    }

    @Test
    void should_throw_on_null() {
        assertThrows(IllegalArgumentException.class, () -> new SupplierAlias(null));
    }

    @Test
    void should_throw_on_blank() {
        assertThrows(IllegalArgumentException.class, () -> new SupplierAlias("   "));
    }

    @Test
    void should_throw_on_empty() {
        assertThrows(IllegalArgumentException.class, () -> new SupplierAlias(""));
    }

    @Test
    void should_throw_on_too_long() {
        String tooLong = "A".repeat(101);
        assertThrows(IllegalArgumentException.class, () -> new SupplierAlias(tooLong));
    }

    @Test
    void should_be_equal_for_same_value() {
        SupplierAlias alias1 = new SupplierAlias("Proveedor");
        SupplierAlias alias2 = new SupplierAlias("Proveedor");
        assertEquals(alias1, alias2);
        assertEquals(alias1.hashCode(), alias2.hashCode());
    }

    @Test
    void should_not_be_equal_for_different_values() {
        SupplierAlias alias1 = new SupplierAlias("Proveedor");
        SupplierAlias alias2 = new SupplierAlias("Other");
        assertNotEquals(alias1, alias2);
    }

    @Test
    void should_return_value_in_toString() {
        SupplierAlias alias = new SupplierAlias("Proveedor");
        assertEquals("Proveedor", alias.toString());
    }
}
