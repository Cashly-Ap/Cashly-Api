package com.cashly.cashly_api.invoices.infrastructure.services;

import com.cashly.cashly_api.invoices.application.ports.out.InvoiceFinancialPositionGateway;
import com.cashly.cashly_api.invoices.domain.entity.Invoice;
import com.cashly.cashly_api.invoices.domain.repository.InvoiceRepository;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

class InvoiceStatusServiceImplTest {
    @Test
    void givenPosition_whenRecalculating_thenUsesTheFinancialTotalsAndPersistsInvoice() {
        InvoiceRepository invoices = mock(InvoiceRepository.class);
        InvoiceFinancialPositionGateway position = mock(InvoiceFinancialPositionGateway.class);
        Invoice invoice = mock(Invoice.class);
        UUID invoiceId = UUID.randomUUID();
        when(invoices.findById(InvoiceId.of(invoiceId))).thenReturn(Optional.of(invoice));
        when(position.findByInvoiceId(invoiceId)).thenReturn(new InvoiceFinancialPositionGateway.Position(
                new BigDecimal("50.00"), new BigDecimal("8.00"), new BigDecimal("3.00")));

        new InvoiceStatusServiceImpl(invoices, position).recalculate(invoiceId);

        verify(invoice).recalculatePaymentStatus(new BigDecimal("50.00"),
                new BigDecimal("8.00"), new BigDecimal("3.00"));
        verify(invoices).save(invoice);
    }
}
