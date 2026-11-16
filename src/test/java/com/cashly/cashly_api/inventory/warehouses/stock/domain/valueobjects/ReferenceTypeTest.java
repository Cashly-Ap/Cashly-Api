package com.cashly.cashly_api.inventory.warehouses.stock.domain.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ReferenceType Enum Unit Tests")
class ReferenceTypeTest {

    @Test
    void should_HaveSixValues() {
        ReferenceType[] values = ReferenceType.values();
        assertEquals(6, values.length);
    }

    @Test
    void should_HaveOrderValue() {
        assertNotNull(ReferenceType.valueOf("ORDER"));
        assertEquals(ReferenceType.ORDER, ReferenceType.valueOf("ORDER"));
    }

    @Test
    void should_HaveInvoiceValue() {
        assertNotNull(ReferenceType.valueOf("INVOICE"));
        assertEquals(ReferenceType.INVOICE, ReferenceType.valueOf("INVOICE"));
    }

    @Test
    void should_HaveReturnValue() {
        assertNotNull(ReferenceType.valueOf("RETURN"));
        assertEquals(ReferenceType.RETURN, ReferenceType.valueOf("RETURN"));
    }

    @Test
    void should_HaveTransferValue() {
        assertNotNull(ReferenceType.valueOf("TRANSFER"));
        assertEquals(ReferenceType.TRANSFER, ReferenceType.valueOf("TRANSFER"));
    }

    @Test
    void should_HavePurchaseValue() {
        assertNotNull(ReferenceType.valueOf("PURCHASE"));
        assertEquals(ReferenceType.PURCHASE, ReferenceType.valueOf("PURCHASE"));
    }

    @Test
    void should_HaveManualValue() {
        assertNotNull(ReferenceType.valueOf("MANUAL"));
        assertEquals(ReferenceType.MANUAL, ReferenceType.valueOf("MANUAL"));
    }

    @Test
    void should_BeCorrectlyOrdered() {
        assertSame(ReferenceType.ORDER, ReferenceType.values()[0]);
        assertSame(ReferenceType.INVOICE, ReferenceType.values()[1]);
        assertSame(ReferenceType.RETURN, ReferenceType.values()[2]);
        assertSame(ReferenceType.TRANSFER, ReferenceType.values()[3]);
        assertSame(ReferenceType.PURCHASE, ReferenceType.values()[4]);
        assertSame(ReferenceType.MANUAL, ReferenceType.values()[5]);
    }
}
