package com.parsun.parsun_api.quotes.domain;

import com.parsun.parsun_api.quotes.domain.valueobjects.QuoteNumber;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class QuoteNumberTest {

    @ParameterizedTest
    @CsvSource({
            "2024, 1, 2024-0001",
            "2024, 100, 2024-0100",
            "2024, 9999, 2024-9999",
            "2025, 1, 2025-0001",
            "2030, 500, 2030-0500"
    })
    void should_create_valid_quote_number(int year, int sequence, String expected) {
        QuoteNumber quoteNumber = QuoteNumber.of(year, sequence);
        assertEquals(expected, quoteNumber.value());
    }

    @Test
    void should_accept_valid_formatted_value() {
        QuoteNumber quoteNumber = new QuoteNumber("2024-0001");
        assertEquals("2024-0001", quoteNumber.value());
    }

    @Test
    void should_accept_max_sequence_value() {
        QuoteNumber quoteNumber = QuoteNumber.of(2024, 9999);
        assertEquals("2024-9999", quoteNumber.value());
    }

    @Test
    void should_throw_on_invalid_format() {
        assertThrows(IllegalArgumentException.class,
                () -> new QuoteNumber("invalid"));
    }

    @Test
    void should_throw_on_wrong_separator() {
        assertThrows(IllegalArgumentException.class,
                () -> new QuoteNumber("2024/0001"));
    }

    @Test
    void should_throw_on_too_short_sequence() {
        assertThrows(IllegalArgumentException.class,
                () -> new QuoteNumber("2024-001"));
    }

    @Test
    void should_throw_on_too_long_sequence() {
        assertThrows(IllegalArgumentException.class,
                () -> new QuoteNumber("2024-00001"));
    }

    @Test
    void should_throw_on_letters_in_value() {
        assertThrows(IllegalArgumentException.class,
                () -> new QuoteNumber("20A4-0001"));
    }

    @Test
    void should_throw_on_null() {
        assertThrows(IllegalArgumentException.class,
                () -> new QuoteNumber(null));
    }

    @Test
    void should_throw_on_blank() {
        assertThrows(IllegalArgumentException.class,
                () -> new QuoteNumber("   "));
    }

    @Test
    void should_throw_on_empty() {
        assertThrows(IllegalArgumentException.class,
                () -> new QuoteNumber(""));
    }

    @Test
    void should_be_equal_for_same_value() {
        QuoteNumber qn1 = QuoteNumber.of(2024, 1);
        QuoteNumber qn2 = QuoteNumber.of(2024, 1);
        assertEquals(qn1, qn2);
        assertEquals(qn1.hashCode(), qn2.hashCode());
    }

    @Test
    void should_not_be_equal_for_different_values() {
        QuoteNumber qn1 = QuoteNumber.of(2024, 1);
        QuoteNumber qn2 = QuoteNumber.of(2024, 2);
        assertNotEquals(qn1, qn2);
    }
}
