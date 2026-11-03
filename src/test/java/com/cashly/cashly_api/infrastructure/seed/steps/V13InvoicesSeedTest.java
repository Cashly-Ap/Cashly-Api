package com.cashly.cashly_api.infrastructure.seed.steps;

import static com.cashly.cashly_api.infrastructure.seed.SeedIds.INVOICE_ONE;
import static com.cashly.cashly_api.infrastructure.seed.SeedIds.id;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cashly.cashly_api.finance.billing.invoices.infrastructure.persistence.entities.InvoiceEntity;
import com.cashly.cashly_api.finance.billing.invoices.infrastructure.persistence.entities.InvoiceLineEntity;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class V13InvoicesSeedTest {

    private final EntityManager entityManager = mock(EntityManager.class);

    @Test
    void recreatesDeterministicLineWhenInvoiceExistsWithoutIt() {
        InvoiceEntity invoice = new InvoiceEntity();
        invoice.setId(INVOICE_ONE);
        invoice.setSubtotal(BigDecimal.ZERO);
        when(entityManager.find(eq(InvoiceEntity.class), any())).thenReturn(null);
        when(entityManager.find(InvoiceEntity.class, INVOICE_ONE))
                .thenReturn(invoice);
        when(entityManager.find(eq(InvoiceLineEntity.class), any()))
                .thenReturn(null);

        new V13InvoicesSeed(entityManager).seed();

        assertThat(invoice.getLines()).hasSize(1);
        assertThat(invoice.getLines().get(0).getId())
                .isEqualTo(id("invoice-line.one"));
        assertThat(invoice.getSubtotal()).isEqualByComparingTo("13.00");
        verify(entityManager, never()).persist(invoice);
    }

    @Test
    void keepsExistingDeterministicLineWithoutDuplicatingIt() {
        InvoiceEntity invoice = new InvoiceEntity();
        invoice.setId(INVOICE_ONE);
        InvoiceLineEntity existingLine = new InvoiceLineEntity();
        existingLine.setId(id("invoice-line.one"));
        invoice.getLines().add(existingLine);
        when(entityManager.find(eq(InvoiceEntity.class), any())).thenReturn(null);
        when(entityManager.find(InvoiceEntity.class, INVOICE_ONE))
                .thenReturn(invoice);
        when(entityManager.find(eq(InvoiceLineEntity.class), any()))
                .thenReturn(null);
        when(entityManager.find(InvoiceLineEntity.class,
                id("invoice-line.one"))).thenReturn(existingLine);

        new V13InvoicesSeed(entityManager).seed();

        assertThat(invoice.getLines()).containsExactly(existingLine);
    }
}
