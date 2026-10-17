package com.cashly.cashly_api.finance.treasury.payments.credit.application.services;

import com.cashly.cashly_api.finance.treasury.payments.credit.application.ports.out.CustomerCreditContextGateway;
import com.cashly.cashly_api.finance.treasury.payments.credit.application.ports.out.model.InvoiceCreditEntry;
import com.cashly.cashly_api.finance.treasury.payments.credit.application.ports.out.InvoiceCreditLedgerGateway;
import com.cashly.cashly_api.finance.treasury.payments.credit.application.ports.out.CreditNoteLedgerGateway;
import com.cashly.cashly_api.finance.treasury.payments.credit.application.ports.out.DebitNoteLedgerGateway;
import com.cashly.cashly_api.finance.treasury.payments.credit.application.ports.in.CreditStatusSnapshot;
import com.cashly.cashly_api.finance.treasury.payments.credit.application.ports.in.OverdueInvoiceSnapshot;
import com.cashly.cashly_api.finance.treasury.payments.credit.application.ports.in.CustomerCreditQuery;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.entity.PaymentAllocation;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentAllocationRepository;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentRepository;
import com.cashly.cashly_api.finance.treasury.payments.credit.domain.services.CustomerCreditPolicy;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.PaymentId;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

public class CustomerCreditQueryService implements CustomerCreditQuery {
    private final CustomerCreditContextGateway customers;
    private final PaymentRepository payments;
    private final PaymentAllocationRepository allocations;
    private final InvoiceCreditLedgerGateway invoices;
    private final CreditNoteLedgerGateway credits;
    private final DebitNoteLedgerGateway debits;

    public CustomerCreditQueryService(CustomerCreditContextGateway customers,
            PaymentRepository payments, PaymentAllocationRepository allocations,
            InvoiceCreditLedgerGateway invoices, CreditNoteLedgerGateway credits,
            DebitNoteLedgerGateway debits) {
        this.customers = customers;
        this.payments = payments;
        this.allocations = allocations;
        this.invoices = invoices;
        this.credits = credits;
        this.debits = debits;
    }

    @Override
    public CreditStatusSnapshot getCreditStatus(UUID customerId) {
        var customer = customers.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));
        var activeInvoices = invoices.findByCustomerId(customerId).stream()
                .filter(InvoiceCreditEntry::pendingPayment).toList();
        BigDecimal invoiceTotal = activeInvoices.stream().map(this::calculateInvoiceTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal pendingBalance = invoiceTotal.subtract(calculateAppliedPayments(activeInvoices));
        var overdueInvoices = overdueInvoices(invoices.findByCustomerId(customerId));
        boolean hasOverdue = !overdueInvoices.isEmpty();
        var decision = new CustomerCreditPolicy().evaluate(customer.creditLimit(), customer.balance(), pendingBalance, hasOverdue);
        return new CreditStatusSnapshot(customerId, customer.name(), customer.rif(), customer.creditLimit(),
                pendingBalance, customer.balance(), decision.availableCredit(), overdueInvoices, hasOverdue, decision.canCreateCreditInvoice());
    }

    @Override
    public List<OverdueInvoiceSnapshot> findAllOverdueInvoices() {
        return overdueInvoices(invoices.findAll());
    }

    private List<OverdueInvoiceSnapshot> overdueInvoices(List<InvoiceCreditEntry> entries) {
        LocalDate now = LocalDate.now(ZoneId.systemDefault());
        return entries.stream().filter(InvoiceCreditEntry::pendingPayment)
                .filter(invoice -> invoice.dueDate().isBefore(now))
                .map(invoice -> new OverdueInvoiceSnapshot(invoice.number(), calculateInvoiceTotal(invoice), invoice.dueDate(),
                        (int) ChronoUnit.DAYS.between(invoice.dueDate(), now))).toList();
    }

    private BigDecimal calculateInvoiceTotal(InvoiceCreditEntry invoice) {
        if (invoice.subtotal() == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal totalCredit = credits.amountsByInvoiceId(invoice.id()).stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalDebit = debits.amountsByInvoiceId(invoice.id()).stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return invoice.subtotal().subtract(invoice.returnedTotal()).subtract(totalCredit).add(totalDebit);
    }

    private BigDecimal calculateAppliedPayments(List<InvoiceCreditEntry> entries) {
        BigDecimal totalApplied = BigDecimal.ZERO;
        for (InvoiceCreditEntry invoice : entries) {
            for (PaymentAllocation allocation : allocations.findByInvoiceId(invoice.id())) {
                var payment = payments.findById(PaymentId.of(allocation.getPaymentId())).orElse(null);
                if (payment != null && payment.getStatus() != PaymentStatus.CANCELLED) {
                    totalApplied = totalApplied.add(allocation.getAllocatedAmount());
                }
            }
        }
        return totalApplied;
    }
}
