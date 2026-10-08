package com.cashly.cashly_api.invoices.infrastructure.integration;

import com.cashly.cashly_api.credit_notes.application.api.InvoiceCreditNoteDetailsQuery;
import com.cashly.cashly_api.debit_notes.application.api.InvoiceDebitNoteDetailsQuery;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LocalInvoiceNoteDetailsAdapterTest {
    @Test
    void mapsNoteDetailsAndBatchTotalsAcrossPublicModuleQueries() {
        InvoiceCreditNoteDetailsQuery credits = mock(InvoiceCreditNoteDetailsQuery.class);
        InvoiceDebitNoteDetailsQuery debits = mock(InvoiceDebitNoteDetailsQuery.class);
        UUID invoiceId = UUID.randomUUID();
        UUID noteId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();
        when(credits.findByInvoiceId(invoiceId)).thenReturn(List.of(new InvoiceCreditNoteDetailsQuery.Note(
                noteId, "CN-1", new BigDecimal("12.50"), "Adjustment", createdAt)));
        when(credits.sumByInvoiceIds(List.of(invoiceId))).thenReturn(Map.of(invoiceId, new BigDecimal("12.50")));
        when(debits.sumByInvoiceIds(List.of(invoiceId))).thenReturn(Map.of());

        var adapter = new LocalInvoiceNoteDetailsAdapter(credits, debits);

        assertThat(adapter.findCreditNotes(invoiceId)).containsExactly(new com.cashly.cashly_api.invoices.application.contracts.InvoiceNoteDetailsGateway.Note(
                noteId, "CN-1", new BigDecimal("12.50"), "Adjustment", createdAt));
        assertThat(adapter.sumCreditNotes(List.of(invoiceId))).containsEntry(invoiceId, new BigDecimal("12.50"));
        assertThat(adapter.sumDebitNotes(List.of(invoiceId))).isEmpty();
    }
}
