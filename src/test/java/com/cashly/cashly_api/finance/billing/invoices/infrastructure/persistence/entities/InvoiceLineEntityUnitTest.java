package com.cashly.cashly_api.finance.billing.invoices.infrastructure.persistence.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.hibernate.annotations.BatchSize;
import org.junit.jupiter.api.Test;

class InvoiceLineEntityUnitTest {

    @Test
    void shouldBatchLoadAllocations() throws NoSuchFieldException {
        var allocations = InvoiceLineEntity.class.getDeclaredField("allocations");
        var batchSize = allocations.getAnnotation(BatchSize.class);

        assertEquals(100, batchSize.size());
    }
}
