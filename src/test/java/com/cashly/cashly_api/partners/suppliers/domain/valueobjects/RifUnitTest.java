package com.cashly.cashly_api.partners.suppliers.domain.valueobjects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class RifUnitTest {

    @ParameterizedTest
    @CsvSource({
            "V-12345678-9, V-12345678-9",
            "J-12345678-9, J-12345678-9",
            "G-12345678-9, G-12345678-9",
            "E-12345678-9, E-12345678-9",
            "C-12345678-9, C-12345678-9",
            "P-12345678-9, P-12345678-9",
            "v-12345678-9, V-12345678-9",
            " j-12345678-9 , J-12345678-9"
    })
    void should_create_valid_rif(String input, String expected) {
        Rif rif = new Rif(input);
        assertEquals(expected, rif.getValue());
    }

    @Test
    void should_throw_on_null() {
        assertThrows(IllegalArgumentException.class, () -> new Rif(null));
    }

    @Test
    void should_throw_on_blank() {
        assertThrows(IllegalArgumentException.class, () -> new Rif("   "));
    }

    @Test
    void should_throw_on_empty() {
        assertThrows(IllegalArgumentException.class, () -> new Rif(""));
    }

    @Test
    void should_throw_on_no_dash() {
        assertThrows(IllegalArgumentException.class, () -> new Rif("V123456789"));
    }

    @Test
    void should_throw_on_wrong_letter() {
        assertThrows(IllegalArgumentException.class, () -> new Rif("X-12345678-9"));
    }

    @Test
    void should_throw_on_wrong_digit_count() {
        assertThrows(IllegalArgumentException.class, () -> new Rif("V-1234567-9"));
        assertThrows(IllegalArgumentException.class, () -> new Rif("V-123456789-9"));
    }

    @Test
    void should_throw_on_missing_check_digit() {
        assertThrows(IllegalArgumentException.class, () -> new Rif("V-12345678"));
    }

    @Test
    void should_be_equal_for_same_value() {
        Rif rif1 = new Rif("V-12345678-9");
        Rif rif2 = new Rif("V-12345678-9");
        assertEquals(rif1, rif2);
        assertEquals(rif1.hashCode(), rif2.hashCode());
    }

    @Test
    void should_not_be_equal_for_different_values() {
        Rif rif1 = new Rif("V-12345678-9");
        Rif rif2 = new Rif("J-12345678-9");
        assertNotEquals(rif1, rif2);
    }

    @Test
    void should_return_value_in_toString() {
        Rif rif = new Rif("V-12345678-9");
        assertEquals("V-12345678-9", rif.toString());
    }
}
