package com.cashly.cashly_api.debit_notes.infrastructure.integration;

import com.cashly.cashly_api.debit_notes.application.contracts.DebitNoteInvoiceGateway;
import com.cashly.cashly_api.invoices.application.api.InvoiceNoteApi;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class LocalDebitNoteInvoiceGatewayTest {
    @Test
    void givenProviderBatch_whenQueryingInvoices_thenMapsNeutralContextAndRecalculation() {
        InvoiceNoteApi invoices = mock(InvoiceNoteApi.class);
        UUID invoiceId = UUID.randomUUID();
        when(invoices.find(invoiceId)).thenReturn(new InvoiceNoteApi.Context(invoiceId, "FAC-1",
                "PAID", "Pagada", "Acme", "RIF", new BigDecimal("100.00")));
        when(invoices.findAll(Set.of(invoiceId))).thenReturn(Map.of(invoiceId,
                new InvoiceNoteApi.Context(invoiceId, "FAC-1", "PAID", "Pagada", "Acme", "RIF",
                        new BigDecimal("100.00"))));
        LocalDebitNoteInvoiceGateway gateway = new LocalDebitNoteInvoiceGateway(invoices);

        var single = gateway.find(invoiceId);
        var batch = gateway.findAll(Set.of(invoiceId)).get(invoiceId);
        gateway.recalculate(invoiceId);

        assertEquals("FAC-1", single.number());
        assertEquals("Acme", batch.customerName());
        assertEquals("RIF", batch.customerRif());
        verify(invoices).recalculate(invoiceId);
    }
}
