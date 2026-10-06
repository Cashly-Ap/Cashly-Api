package com.cashly.cashly_api.customers.domain.valueobjects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CreditDaysUnitTest {

    @Test
    void should_create_zero_days() {
        CreditDays days = new CreditDays(0);
        assertEquals(0, days.getValue());
    }

    @Test
    void should_create_valid_days() {
        CreditDays days = new CreditDays(30);
        assertEquals(30, days.getValue());
    }

    @Test
    void should_create_maximum_days() {
        CreditDays days = new CreditDays(365);
        assertEquals(365, days.getValue());
    }

    @Test
    void should_throw_on_negative() {
        assertThrows(IllegalArgumentException.class, () -> new CreditDays(-1));
    }

    @Test
    void should_throw_on_too_high() {
        assertThrows(IllegalArgumentException.class, () -> new CreditDays(366));
    }

    @Test
    void should_be_equal_for_same_value() {
        CreditDays days1 = new CreditDays(30);
        CreditDays days2 = new CreditDays(30);
        assertEquals(days1, days2);
        assertEquals(days1.hashCode(), days2.hashCode());
    }

    @Test
    void should_not_be_equal_for_different_values() {
        CreditDays days1 = new CreditDays(30);
        CreditDays days2 = new CreditDays(60);
        assertNotEquals(days1, days2);
    }

    @Test
    void should_return_value_in_toString() {
        CreditDays days = new CreditDays(30);
        assertEquals("30", days.toString());
    }
}
