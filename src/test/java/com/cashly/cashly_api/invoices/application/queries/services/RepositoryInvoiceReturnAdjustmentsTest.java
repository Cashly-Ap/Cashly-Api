package com.cashly.cashly_api.invoices.application.queries.services;

import com.cashly.cashly_api.invoices.application.ports.in.InvoiceReturnCommand;

import com.cashly.cashly_api.invoices.domain.entity.Invoice;
import com.cashly.cashly_api.invoices.domain.exceptions.InvoiceNotFoundException;
import com.cashly.cashly_api.invoices.domain.repository.InvoiceRepository;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceId;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceReturnAdjustment;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class RepositoryInvoiceReturnAdjustmentsTest {
    @Test
    void shouldReverseOwnedAdjustmentAndRecalculateBeforeSaving() {
        InvoiceRepository repository = mock(InvoiceRepository.class);
        Invoice invoice = mock(Invoice.class);
        InvoiceReturnCommand command = new InvoiceReturnCommand(UUID.randomUUID(), BigDecimal.TEN, List.of(new InvoiceReturnCommand.Line(UUID.randomUUID(), BigDecimal.ONE)), true);
        when(repository.findById(InvoiceId.of(command.invoiceId()))).thenReturn(Optional.of(invoice));

        new RepositoryInvoiceReturnAdjustments(repository).reverse(command, BigDecimal.TEN, BigDecimal.ONE, BigDecimal.ZERO);

        var order = inOrder(invoice, repository);
        order.verify(invoice).revertReturn(new InvoiceReturnAdjustment(command.invoiceId(), BigDecimal.TEN, List.of(new InvoiceReturnAdjustment.Line(command.lines().getFirst().invoiceLineId(), BigDecimal.ONE)), true));
        order.verify(invoice).recalculatePaymentStatus(BigDecimal.TEN, BigDecimal.ONE, BigDecimal.ZERO);
        order.verify(repository).save(invoice);
    }

    @Test
    void shouldRejectMissingInvoiceWithoutSaving() {
        InvoiceRepository repository = mock(InvoiceRepository.class);
        InvoiceReturnCommand command = new InvoiceReturnCommand(UUID.randomUUID(), BigDecimal.TEN, List.of(new InvoiceReturnCommand.Line(UUID.randomUUID(), BigDecimal.ONE)), false);
        when(repository.findById(InvoiceId.of(command.invoiceId()))).thenReturn(Optional.empty());

        assertThrows(InvoiceNotFoundException.class, () -> new RepositoryInvoiceReturnAdjustments(repository).apply(command));
        verify(repository, never()).save(any());
    }
}
