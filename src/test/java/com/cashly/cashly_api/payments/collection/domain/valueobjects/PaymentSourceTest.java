package com.cashly.cashly_api.payments.collection.domain.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PaymentSource Enum Unit Tests")
class PaymentSourceTest {

    @Test
    void should_HaveTwoValues() {
        PaymentSource[] values = PaymentSource.values();
        assertEquals(2, values.length);
    }

    @Test
    void should_HavePaymentMethodValue() {
        assertNotNull(PaymentSource.valueOf("PAYMENT_METHOD"));
        assertEquals(PaymentSource.PAYMENT_METHOD, PaymentSource.valueOf("PAYMENT_METHOD"));
    }

    @Test
    void should_HaveCustomerBalanceValue() {
        assertNotNull(PaymentSource.valueOf("CUSTOMER_BALANCE"));
        assertEquals(PaymentSource.CUSTOMER_BALANCE, PaymentSource.valueOf("CUSTOMER_BALANCE"));
    }

    @Test
    void should_BeCorrectlyOrdered() {
        assertSame(PaymentSource.PAYMENT_METHOD, PaymentSource.values()[0]);
        assertSame(PaymentSource.CUSTOMER_BALANCE, PaymentSource.values()[1]);
    }
}
