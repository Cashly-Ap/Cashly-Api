package com.cashly.cashly_api.infrastructure.seed.steps;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.cashly.cashly_api.finance.billing.invoices.infrastructure.persistence.entities.InvoiceLineAllocationEntity;
import com.cashly.cashly_api.finance.billing.invoices.infrastructure.persistence.entities.InvoiceLineEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

class V14InvoiceAllocationsSeedTest {

    private final EntityManager entityManager = mock(EntityManager.class);

    @Test
    void skipsAllocationWhenInvoiceLineIsMissing() {
        doReturn(null).when(entityManager)
                .find(eq(InvoiceLineAllocationEntity.class), any());
        doReturn(null).when(entityManager).find(eq(InvoiceLineEntity.class),
                any());
        var seed = new V14InvoiceAllocationsSeed(entityManager);

        assertThatCode(seed::seed).doesNotThrowAnyException();
        verify(entityManager, never()).persist(any());
    }

    @Test
    void persistsAllocationWhenInvoiceLineExists() {
        doReturn(null).when(entityManager)
                .find(eq(InvoiceLineAllocationEntity.class), any());
        doReturn(mock(InvoiceLineEntity.class)).when(entityManager)
                .find(eq(InvoiceLineEntity.class), any());
        var seed = new V14InvoiceAllocationsSeed(entityManager);

        seed.seed();

        verify(entityManager, times(30))
                .persist(any(InvoiceLineAllocationEntity.class));
    }
}
