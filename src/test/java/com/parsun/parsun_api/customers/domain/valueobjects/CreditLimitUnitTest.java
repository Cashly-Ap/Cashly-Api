package com.parsun.parsun_api.customers.domain.valueobjects;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CreditLimitUnitTest {

    @Test
    void should_create_zero_limit() {
        CreditLimit limit = new CreditLimit(BigDecimal.ZERO);
        assertEquals(BigDecimal.ZERO, limit.getValue());
    }

    @Test
    void should_create_positive_limit() {
        BigDecimal value = new BigDecimal("1000.00");
        CreditLimit limit = new CreditLimit(value);
        assertEquals(value, limit.getValue());
    }

    @Test
    void should_create_large_limit() {
        BigDecimal value = new BigDecimal("999999.99");
        CreditLimit limit = new CreditLimit(value);
        assertEquals(value, limit.getValue());
    }

    @Test
    void should_throw_on_null() {
        assertThrows(IllegalArgumentException.class, () -> new CreditLimit(null));
    }

    @Test
    void should_throw_on_negative() {
        assertThrows(IllegalArgumentException.class, () -> new CreditLimit(new BigDecimal("-0.01")));
    }

    @Test
    void should_be_equal_for_same_value() {
        BigDecimal value = new BigDecimal("1000.00");
        CreditLimit limit1 = new CreditLimit(value);
        CreditLimit limit2 = new CreditLimit(value);
        assertEquals(limit1, limit2);
        assertEquals(limit1.hashCode(), limit2.hashCode());
    }

    @Test
    void should_not_be_equal_for_different_values() {
        CreditLimit limit1 = new CreditLimit(new BigDecimal("1000.00"));
        CreditLimit limit2 = new CreditLimit(new BigDecimal("2000.00"));
        assertNotEquals(limit1, limit2);
    }

    @Test
    void should_return_value_in_toString() {
        BigDecimal value = new BigDecimal("1000.00");
        CreditLimit limit = new CreditLimit(value);
        assertEquals("1000.00", limit.toString());
    }
}
