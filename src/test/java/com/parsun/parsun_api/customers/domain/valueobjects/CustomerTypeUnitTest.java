package com.parsun.parsun_api.customers.domain.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CustomerType Enum Unit Tests")
class CustomerTypeUnitTest {

    @Test
    void should_HaveFourValues() {
        CustomerType[] values = CustomerType.values();
        assertEquals(4, values.length);
    }

    @Test
    void should_HaveIndividualValue() {
        assertNotNull(CustomerType.valueOf("INDIVIDUAL"));
        assertEquals(CustomerType.INDIVIDUAL, CustomerType.valueOf("INDIVIDUAL"));
    }

    @Test
    void should_HaveBusinessValue() {
        assertNotNull(CustomerType.valueOf("BUSINESS"));
        assertEquals(CustomerType.BUSINESS, CustomerType.valueOf("BUSINESS"));
    }

    @Test
    void should_HaveGovernmentValue() {
        assertNotNull(CustomerType.valueOf("GOVERNMENT"));
        assertEquals(CustomerType.GOVERNMENT, CustomerType.valueOf("GOVERNMENT"));
    }

    @Test
    void should_HaveNonProfitValue() {
        assertNotNull(CustomerType.valueOf("NON_PROFIT"));
        assertEquals(CustomerType.NON_PROFIT, CustomerType.valueOf("NON_PROFIT"));
    }

    @Test
    void should_BeOrderedCorrectly() {
        assertSame(CustomerType.INDIVIDUAL, CustomerType.values()[0]);
        assertSame(CustomerType.BUSINESS, CustomerType.values()[1]);
        assertSame(CustomerType.GOVERNMENT, CustomerType.values()[2]);
        assertSame(CustomerType.NON_PROFIT, CustomerType.values()[3]);
    }
}
