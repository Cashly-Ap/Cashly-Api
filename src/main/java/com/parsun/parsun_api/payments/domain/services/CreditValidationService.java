package com.parsun.parsun_api.payments.domain.services;

import com.parsun.parsun_api.customers.domain.entities.Customer;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import com.parsun.parsun_api.customers.domain.valueobjects.CustomerId;
import com.parsun.parsun_api.credit_notes.domain.entities.CreditNote;
import com.parsun.parsun_api.debit_notes.domain.entities.DebitNote;
import com.parsun.parsun_api.credit_notes.domain.repositories.CreditNoteRepository;
import com.parsun.parsun_api.debit_notes.domain.repositories.DebitNoteRepository;
import com.parsun.parsun_api.receivables.application.dto.response.AdvanceResponse;
import com.parsun.parsun_api.receivables.application.dto.response.CreditStatusResponse;
import com.parsun.parsun_api.receivables.application.dto.response.OverdueInvoiceResponse;
import com.parsun.parsun_api.payments.domain.entities.PaymentAllocation;
import com.parsun.parsun_api.payments.domain.repositories.PaymentAllocationRepository;
import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceStatus;
import com.parsun.parsun_api.payments.domain.entities.Payment;
import com.parsun.parsun_api.payments.domain.repositories.PaymentRepository;
import com.parsun.parsun_api.payments.domain.valueobjects.PaymentId;
import com.parsun.parsun_api.payments.domain.valueobjects.PaymentStatus;
import com.parsun.parsun_api.payments.domain.valueobjects.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CreditValidationService implements com.parsun.parsun_api.shared.services.CreditValidationService {

    private final CustomerRepository customerRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentAllocationRepository paymentAllocationRepository;
    private final InvoiceRepository invoiceRepository;
    private final CreditNoteRepository creditNoteRepository;
    private final DebitNoteRepository debitNoteRepository;

    public CreditValidationService(CustomerRepository customerRepository,
                                    PaymentRepository paymentRepository,
                                    PaymentAllocationRepository paymentAllocationRepository,
                                    InvoiceRepository invoiceRepository,
                                    CreditNoteRepository creditNoteRepository,
                                    DebitNoteRepository debitNoteRepository) {
        this.customerRepository = customerRepository;
        this.paymentRepository = paymentRepository;
        this.paymentAllocationRepository = paymentAllocationRepository;
        this.invoiceRepository = invoiceRepository;
        this.creditNoteRepository = creditNoteRepository;
        this.debitNoteRepository = debitNoteRepository;
    }

    @Override
    public CreditStatusResponse getCreditStatus(UUID customerId) {
        Customer customer = customerRepository.findById(new CustomerId(customerId))
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));

        BigDecimal creditLimit = customer.getCreditLimit().getValue();
        int creditDays = customer.getCreditDays().getValue();

        BigDecimal pendingBalance = calculatePendingBalance(customerId);
        BigDecimal unconsumedAdvances = calculateUnconsumedAdvances(customerId);
        BigDecimal availableCredit = creditLimit.add(unconsumedAdvances).subtract(pendingBalance);

        List<OverdueInvoiceResponse> overdueInvoices = findOverdueInvoices(customerId, creditDays);
        List<AdvanceResponse> advances = findActiveAdvances(customerId);

        boolean hasOverdue = !overdueInvoices.isEmpty();
        boolean canCreateCreditInvoice = availableCredit.compareTo(BigDecimal.ZERO) >= 0 && !hasOverdue;

        return new CreditStatusResponse(
                customerId,
                customer.getLegalName().getValue(),
                customer.getRif().getValue(),
                creditLimit,
                pendingBalance,
                unconsumedAdvances,
                availableCredit,
                overdueInvoices,
                advances,
                hasOverdue,
                canCreateCreditInvoice
        );
    }

    @Override
    public List<OverdueInvoiceResponse> findAllOverdueInvoices() {
        List<Invoice> allInvoices = invoiceRepository.findAllWithFilters(null, null, null, null, null, null, null, "createdAt", "desc");

        List<OverdueInvoiceResponse> result = new ArrayList<>();
        LocalDate now = LocalDate.now(ZoneId.systemDefault());

        for (Invoice invoice : allInvoices) {
            if (invoice.getStatus() == InvoiceStatus.FULLY_RETURNED
                || (invoice.getStatus() != InvoiceStatus.PENDING_PAYMENT && invoice.getStatus() != InvoiceStatus.PARTIALLY_PAID)) {
                continue;
            }

            Customer customer = customerRepository.findById(new CustomerId(invoice.getCustomerId()))
                    .orElse(null);
            if (customer == null) {
                continue;
            }

            int creditDays = customer.getCreditDays().getValue();
            LocalDate invoiceDate = invoice.getCreatedAt().toLocalDate();
            LocalDate dueDate = invoiceDate.plusDays(creditDays);

            if (dueDate.isBefore(now)) {
                int daysOverdue = (int) java.time.temporal.ChronoUnit.DAYS.between(dueDate, now);
                BigDecimal totalCredit = creditNoteRepository.findByInvoiceId(invoice.getId().value())
                        .stream().map(CreditNote::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal totalDebit = debitNoteRepository.findByInvoiceId(invoice.getId().value())
                        .stream().map(DebitNote::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                result.add(new OverdueInvoiceResponse(
                        invoice.getInvoiceNumber().value(),
                        invoice.calculateTotal(totalCredit, totalDebit),
                        dueDate,
                        daysOverdue
                ));
            }
        }

        return result;
    }

    private BigDecimal calculatePendingBalance(UUID customerId) {
        List<Invoice> allInvoices = invoiceRepository.findAllWithFilters(null, null, customerId, null, null, null, null, "createdAt", "desc");

        List<Invoice> activeInvoices = allInvoices.stream()
                .filter(inv -> inv.getStatus() != InvoiceStatus.FULLY_RETURNED)
                .toList();

        BigDecimal invoiceTotal = activeInvoices.stream()
                .filter(inv -> inv.getStatus() == InvoiceStatus.PENDING_PAYMENT || inv.getStatus() == InvoiceStatus.PARTIALLY_PAID)
                .map(inv -> {
                    BigDecimal totalCredit = creditNoteRepository.findByInvoiceId(inv.getId().value())
                            .stream().map(CreditNote::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal totalDebit = debitNoteRepository.findByInvoiceId(inv.getId().value())
                            .stream().map(DebitNote::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                    return inv.calculateTotal(totalCredit, totalDebit);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal appliedPayments = calculateAppliedPaymentsForInvoices(activeInvoices);

        return invoiceTotal.subtract(appliedPayments);
    }

    private BigDecimal calculateAppliedPaymentsForInvoices(List<Invoice> invoices) {
        List<UUID> invoiceIds = invoices.stream()
                .map(Invoice::getId)
                .map(id -> id.value())
                .toList();

        BigDecimal totalApplied = BigDecimal.ZERO;
        for (UUID invoiceId : invoiceIds) {
            List<PaymentAllocation> allocations = paymentAllocationRepository.findByInvoiceId(invoiceId);
            for (PaymentAllocation allocation : allocations) {
                Payment payment = paymentRepository.findById(PaymentId.of(allocation.getPaymentId()))
                        .orElse(null);
                if (payment != null && payment.getStatus() != PaymentStatus.CANCELLED) {
                    totalApplied = totalApplied.add(allocation.getAllocatedAmount());
                }
            }
        }
        return totalApplied;
    }

    private BigDecimal calculateUnconsumedAdvances(UUID customerId) {
        List<Payment> allPayments = paymentRepository.findAllWithFilters(null, null, customerId, "ADVANCE", null);

        BigDecimal unconsumed = BigDecimal.ZERO;
        for (Payment payment : allPayments) {
            if (payment.getType() == PaymentType.ADVANCE && payment.getStatus() != PaymentStatus.CANCELLED) {
                unconsumed = unconsumed.add(payment.getAmount().subtract(payment.getAppliedAmount()));
            }
        }
        return unconsumed;
    }

    private List<OverdueInvoiceResponse> findOverdueInvoices(UUID customerId, int creditDays) {
        List<Invoice> allInvoices = invoiceRepository.findAllWithFilters(null, null, customerId, null, null, null, null, "createdAt", "desc");
        List<OverdueInvoiceResponse> result = new ArrayList<>();
        LocalDate now = LocalDate.now(ZoneId.systemDefault());

        for (Invoice invoice : allInvoices) {
            if (invoice.getStatus() == InvoiceStatus.FULLY_RETURNED
                || (invoice.getStatus() != InvoiceStatus.PENDING_PAYMENT && invoice.getStatus() != InvoiceStatus.PARTIALLY_PAID)) {
                continue;
            }

            LocalDate invoiceDate = invoice.getCreatedAt().toLocalDate();
            LocalDate dueDate = invoiceDate.plusDays(creditDays);

            if (dueDate.isBefore(now)) {
                int daysOverdue = (int) java.time.temporal.ChronoUnit.DAYS.between(dueDate, now);
                BigDecimal totalCredit = creditNoteRepository.findByInvoiceId(invoice.getId().value())
                        .stream().map(CreditNote::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal totalDebit = debitNoteRepository.findByInvoiceId(invoice.getId().value())
                        .stream().map(DebitNote::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                result.add(new OverdueInvoiceResponse(
                        invoice.getInvoiceNumber().value(),
                        invoice.calculateTotal(totalCredit, totalDebit),
                        dueDate,
                        daysOverdue
                ));
            }
        }

        return result;
    }

    private List<AdvanceResponse> findActiveAdvances(UUID customerId) {
        List<Payment> allPayments = paymentRepository.findAllWithFilters(null, null, customerId, "ADVANCE", null);
        List<AdvanceResponse> result = new ArrayList<>();

        for (Payment payment : allPayments) {
            if (payment.getType() == PaymentType.ADVANCE && payment.getStatus() != PaymentStatus.CANCELLED) {
                BigDecimal remaining = payment.getAmount().subtract(payment.getAppliedAmount());
                if (remaining.compareTo(BigDecimal.ZERO) > 0) {
                    result.add(new AdvanceResponse(
                            payment.getPaymentNumber().value(),
                            payment.getAmount(),
                            remaining,
                            payment.getPaymentDate()
                    ));
                }
            }
        }

        return result;
    }
}
