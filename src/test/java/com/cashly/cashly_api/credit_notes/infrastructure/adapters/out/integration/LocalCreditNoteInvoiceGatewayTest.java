package com.cashly.cashly_api.credit_notes.infrastructure.adapters.out.integration;

import com.cashly.cashly_api.credit_notes.application.ports.out.CreditNoteInvoiceGateway;
import com.cashly.cashly_api.invoices.application.ports.in.InvoiceNoteApi;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class LocalCreditNoteInvoiceGatewayTest {
    @Test
    void givenProviderContext_whenFindingInvoice_thenMapsNeutralFieldsAndDelegatesRecalculation() {
        InvoiceNoteApi provider = mock(InvoiceNoteApi.class);
        LocalCreditNoteInvoiceGateway gateway = new LocalCreditNoteInvoiceGateway(provider);
        UUID invoiceId = UUID.randomUUID();
        when(provider.find(invoiceId)).thenReturn(new InvoiceNoteApi.Context(
                invoiceId, "FAC-1", "PENDING_PAYMENT", "Pendiente", "Customer", "RIF",
                new BigDecimal("80.00")));

        CreditNoteInvoiceGateway.CreditNoteInvoiceContext result = gateway.find(invoiceId);
        gateway.recalculate(invoiceId);

        assertEquals("FAC-1", result.number());
        assertEquals("PENDING_PAYMENT", result.status());
        assertEquals("Pendiente", result.statusLabel());
        assertEquals(new BigDecimal("80.00"), result.totalBeforeNotes());
        verify(provider).recalculate(invoiceId);
    }
}
