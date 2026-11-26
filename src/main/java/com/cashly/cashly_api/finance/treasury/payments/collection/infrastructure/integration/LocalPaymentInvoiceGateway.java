package com.cashly.cashly_api.finance.treasury.payments.collection.infrastructure.integration;

import com.cashly.cashly_api.sales.invoices.credit_notes.application.ports.in.CreditNoteLedgerQuery;
import com.cashly.cashly_api.sales.invoices.debit_notes.application.ports.in.DebitNoteLedgerQuery;
import com.cashly.cashly_api.sales.invoices.invoices.domain.repository.InvoiceRepository;
import com.cashly.cashly_api.sales.invoices.invoices.domain.services.InvoiceStatusService;
import com.cashly.cashly_api.sales.invoices.invoices.domain.valueobjects.InvoiceId;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.ports.out.PaymentInvoiceGateway;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
public class LocalPaymentInvoiceGateway implements PaymentInvoiceGateway {
    private static final List<String> UNPAID_STATUSES = List
            .of("PENDING_PAYMENT", "PARTIALLY_PAID");

    private final InvoiceRepository invoices;
    private final InvoiceStatusService statuses;
    private final CreditNoteLedgerQuery credits;
    private final DebitNoteLedgerQuery debits;

    public LocalPaymentInvoiceGateway(InvoiceRepository invoices,
            InvoiceStatusService statuses, CreditNoteLedgerQuery credits,
            DebitNoteLedgerQuery debits) {
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
        var found = invoices.findHeaders(customerId, UNPAID_STATUSES);
        if (found.isEmpty()) {
            return List.of();
        }
        List<UUID> invoiceIds = found.stream()
                .map(InvoiceRepository.InvoiceHeader::id).toList();
        Map<UUID, BigDecimal> creditTotals = credits
                .totalsByInvoiceIds(invoiceIds);
        Map<UUID, BigDecimal> debitTotals = debits
                .totalsByInvoiceIds(invoiceIds);
        return found.stream()
                .map(header -> new InvoiceSnapshot(header.id(),
                        header.customerId(), header.number(),
                        total(header, creditTotals.getOrDefault(header.id(),
                                BigDecimal.ZERO),
                                debitTotals.getOrDefault(header.id(),
                                        BigDecimal.ZERO))))
                .toList();
    }

    private static BigDecimal total(InvoiceRepository.InvoiceHeader header,
            BigDecimal creditTotal, BigDecimal debitTotal) {
        BigDecimal subtotal = header.subtotal() == null
                ? BigDecimal.ZERO
                : header.subtotal();
        BigDecimal returned = header.returnedTotal() == null
                ? BigDecimal.ZERO
                : header.returnedTotal();
        return subtotal.subtract(returned).subtract(creditTotal)
                .add(debitTotal);
    }

    @Override
    public void recalculateStatus(UUID invoiceId) {
        statuses.recalculate(invoiceId);
    }

    @Override
    public void updateStatuses(java.util.Collection<UUID> invoiceIds,
            String status) {
        invoices.updateStatuses(invoiceIds, status);
    }

    private InvoiceSnapshot snapshot(
            com.cashly.cashly_api.sales.invoices.invoices.domain.entity.Invoice invoice) {
        UUID id = invoice.getId().value();
        return snapshot(invoice,
                Map.of(id, sum(credits.amountsByInvoiceId(id))),
                Map.of(id, sum(debits.amountsByInvoiceId(id))));
    }

    private static BigDecimal sum(List<BigDecimal> amounts) {
        return amounts.stream()
                .map(amount -> amount == null ? BigDecimal.ZERO : amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private InvoiceSnapshot snapshot(
            com.cashly.cashly_api.sales.invoices.invoices.domain.entity.Invoice invoice,
            Map<UUID, BigDecimal> credits, Map<UUID, BigDecimal> debits) {
        UUID id = invoice.getId().value();
        return new InvoiceSnapshot(id, invoice.getCustomerId(),
                invoice.getInvoiceNumber().value(),
                invoice.calculateTotal(credits.getOrDefault(id, BigDecimal.ZERO),
                        debits.getOrDefault(id, BigDecimal.ZERO)));
    }
}
