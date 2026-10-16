package com.cashly.cashly_api.invoices.infrastructure.integration;

import com.cashly.cashly_api.invoices.domain.entity.Invoice;
import com.cashly.cashly_api.invoices.domain.repository.InvoiceRepository;
import com.cashly.cashly_api.invoices.domain.services.InvoiceStatusService;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceId;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceNumber;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class LocalInvoiceNoteApiTest {
    @Test
    void givenInvoice_whenReadingByIdOrBatch_thenReturnsNeutralSnapshot() {
        InvoiceRepository invoices = mock(InvoiceRepository.class);
        InvoiceStatusService statuses = mock(InvoiceStatusService.class);
        Invoice invoice = mock(Invoice.class);
        InvoiceNumber number = mock(InvoiceNumber.class);
        UUID invoiceId = UUID.randomUUID();
        when(invoice.getId()).thenReturn(InvoiceId.of(invoiceId));
        when(invoice.getInvoiceNumber()).thenReturn(number);
        when(number.value()).thenReturn("FAC-1");
        when(invoice.getStatus()).thenReturn(com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceStatus.PAID);
        when(invoice.calculateTotal(BigDecimal.ZERO, BigDecimal.ZERO)).thenReturn(new BigDecimal("75.00"));
        when(invoice.getCustomerName()).thenReturn("Acme");
        when(invoice.getCustomerRif()).thenReturn("RIF-1");
        when(invoices.findById(InvoiceId.of(invoiceId))).thenReturn(Optional.of(invoice));
        when(invoices.findAllById(List.of(InvoiceId.of(invoiceId)))).thenReturn(List.of(invoice));
        LocalInvoiceNoteApi api = new LocalInvoiceNoteApi(invoices, statuses);

        var single = api.find(invoiceId);
        var batch = api.findAll(Set.of(invoiceId)).get(invoiceId);
        api.recalculate(invoiceId);

        assertEquals("FAC-1", single.number());
        assertEquals("Acme", single.customerName());
        assertEquals("RIF-1", single.customerRif());
        assertEquals(new BigDecimal("75.00"), batch.totalBeforeNotes());
        verify(statuses).recalculate(invoiceId);
    }
}
