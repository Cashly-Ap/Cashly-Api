package com.cashly.cashly_api.customers.profile.domain.valueobjects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PaymentTypeUnitTest {

    @Test
    void should_have_cash_value() {
        PaymentType type = PaymentType.valueOf("CASH");
        assertEquals(PaymentType.CASH, type);
    }

    @Test
    void should_have_credit_value() {
        PaymentType type = PaymentType.valueOf("CREDIT");
        assertEquals(PaymentType.CREDIT, type);
    }

    @Test
    void should_return_two_values() {
        PaymentType[] values = PaymentType.values();
        assertEquals(2, values.length);
    }

    @Test
    void should_contain_cash_in_values() {
        PaymentType[] values = PaymentType.values();
        assertTrue(java.util.Arrays.asList(values).contains(PaymentType.CASH));
    }

    @Test
    void should_contain_credit_in_values() {
        PaymentType[] values = PaymentType.values();
        assertTrue(java.util.Arrays.asList(values).contains(PaymentType.CREDIT));
    }
}
