package com.cashly.cashly_api.payments.infrastructure.adapters.out.integration;

import com.cashly.cashly_api.credit_notes.application.ports.in.CreditNoteLedgerQuery;
import com.cashly.cashly_api.debit_notes.application.ports.in.DebitNoteLedgerQuery;
import com.cashly.cashly_api.invoices.domain.repositories.InvoiceRepository;
import com.cashly.cashly_api.invoices.domain.services.InvoiceStatusService;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceId;
import com.cashly.cashly_api.payments.application.ports.out.PaymentInvoiceGateway;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class LocalPaymentInvoiceGateway implements PaymentInvoiceGateway {
    private static final List<String> UNPAID_STATUSES = List.of("PENDING_PAYMENT", "PARTIALLY_PAID");

    private final InvoiceRepository invoices;
    private final InvoiceStatusService statuses;
    private final CreditNoteLedgerQuery credits;
    private final DebitNoteLedgerQuery debits;

    public LocalPaymentInvoiceGateway(InvoiceRepository invoices, InvoiceStatusService statuses,
                                     CreditNoteLedgerQuery credits, DebitNoteLedgerQuery debits) {
        this.invoices = invoices;
        this.statuses = statuses;
        this.credits = credits;
        this.debits = debits;
    }

    @Override
    public Optional<InvoiceSnapshot> findById(UUID invoiceId) {
        return invoices.findById(InvoiceId.of(invoiceId)).map(this::snapshot);
    }

    @Override
    public List<InvoiceSnapshot> findUnpaidByCustomerId(UUID customerId) {
        return invoices.findAllWithFilters(null, null, customerId, UNPAID_STATUSES,
                null, null, null, null, null).stream().map(this::snapshot).toList();
    }

    @Override
    public void recalculateStatus(UUID invoiceId) {
        statuses.recalculate(invoiceId);
    }

    private InvoiceSnapshot snapshot(com.cashly.cashly_api.invoices.domain.entities.Invoice invoice) {
        UUID id = invoice.getId().value();
        BigDecimal creditTotal = credits.amountsByInvoiceId(id).stream()
                .map(amount -> amount == null ? BigDecimal.ZERO : amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal debitTotal = debits.amountsByInvoiceId(id).stream()
                .map(amount -> amount == null ? BigDecimal.ZERO : amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new InvoiceSnapshot(id, invoice.getCustomerId(), invoice.getInvoiceNumber().value(),
                invoice.calculateTotal(creditTotal, debitTotal));
    }
}
