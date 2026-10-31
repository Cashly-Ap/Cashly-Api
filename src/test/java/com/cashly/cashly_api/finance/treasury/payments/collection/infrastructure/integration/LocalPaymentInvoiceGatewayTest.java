package com.cashly.cashly_api.finance.treasury.payments.collection.infrastructure.integration;

import com.cashly.cashly_api.finance.billing.credit_notes.application.ports.in.CreditNoteLedgerQuery;
import com.cashly.cashly_api.finance.billing.debit_notes.application.ports.in.DebitNoteLedgerQuery;
import com.cashly.cashly_api.finance.billing.invoices.domain.entity.Invoice;
import com.cashly.cashly_api.finance.billing.invoices.domain.entity.InvoiceLine;
import com.cashly.cashly_api.finance.billing.invoices.domain.repository.InvoiceRepository;
import com.cashly.cashly_api.finance.billing.invoices.domain.services.InvoiceStatusService;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceId;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceNumber;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceStatus;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.PaymentType;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.ports.out.PaymentInvoiceGateway;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LocalPaymentInvoiceGatewayTest {
    @Test
    void snapshot_uses_invoice_total_after_credit_and_debit_notes() {
        UUID invoiceId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Invoice invoice = invoice(invoiceId, customerId);
        InvoiceRepository invoices = mock(InvoiceRepository.class);
        CreditNoteLedgerQuery credits = mock(CreditNoteLedgerQuery.class);
        DebitNoteLedgerQuery debits = mock(DebitNoteLedgerQuery.class);
        LocalPaymentInvoiceGateway gateway = new LocalPaymentInvoiceGateway(
                invoices, mock(InvoiceStatusService.class), credits, debits);
        when(invoices.findById(InvoiceId.of(invoiceId)))
                .thenReturn(Optional.of(invoice));
        when(credits.amountsByInvoiceId(invoiceId))
                .thenReturn(List.of(new BigDecimal("50.00")));
        when(debits.amountsByInvoiceId(invoiceId))
                .thenReturn(List.of(new BigDecimal("20.00")));

        PaymentInvoiceGateway.InvoiceSnapshot snapshot = gateway
                .findById(invoiceId).orElseThrow();

        assertEquals(new BigDecimal("470.00"), snapshot.total());
        assertEquals(invoiceId, snapshot.id());
        assertEquals(customerId, snapshot.customerId());
    }

    @Test
    void status_recalculation_is_delegated_to_invoice_module() {
        UUID invoiceId = UUID.randomUUID();
        InvoiceStatusService statuses = mock(InvoiceStatusService.class);
        LocalPaymentInvoiceGateway gateway = new LocalPaymentInvoiceGateway(
                mock(InvoiceRepository.class), statuses,
                mock(CreditNoteLedgerQuery.class),
                mock(DebitNoteLedgerQuery.class));

        gateway.recalculateStatus(invoiceId);

        verify(statuses).recalculate(invoiceId);
    }

    private Invoice invoice(UUID invoiceId, UUID customerId) {
        return Invoice.reconstitute(InvoiceId.of(invoiceId),
                InvoiceNumber.of(2026, 1), customerId, "Test Customer",
                "J-12345678-9", UUID.randomUUID(), "Creator", null, null,
                InvoiceStatus.PENDING_PAYMENT, PaymentType.CREDIT, null,
                BigDecimal.ZERO, null, null, null,
                List.of(InvoiceLine.create(UUID.randomUUID(), "Product", "P001",
                        BigDecimal.ONE, new BigDecimal("500.00"), List.of())),
                LocalDate.now().atStartOfDay(), LocalDate.now().atStartOfDay());
    }
}
