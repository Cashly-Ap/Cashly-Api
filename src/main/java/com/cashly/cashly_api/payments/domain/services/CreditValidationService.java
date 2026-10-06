package com.cashly.cashly_api.payments.domain.services;

import com.cashly.cashly_api.customers.domain.entities.Customer;
import com.cashly.cashly_api.customers.domain.entities.CustomerBalance;
import com.cashly.cashly_api.customers.domain.repositories.CustomerBalanceRepository;
import com.cashly.cashly_api.customers.domain.repositories.CustomerRepository;
import com.cashly.cashly_api.customers.domain.valueobjects.CustomerId;
import com.cashly.cashly_api.credit_notes.domain.entities.CreditNote;
import com.cashly.cashly_api.debit_notes.domain.entities.DebitNote;
import com.cashly.cashly_api.credit_notes.domain.repositories.CreditNoteRepository;
import com.cashly.cashly_api.debit_notes.domain.repositories.DebitNoteRepository;
import com.cashly.cashly_api.customers.application.dto.response.CreditStatusResponse;
import com.cashly.cashly_api.customers.application.dto.response.OverdueInvoiceResponse;
import com.cashly.cashly_api.payments.domain.entities.PaymentAllocation;
import com.cashly.cashly_api.payments.domain.repositories.PaymentAllocationRepository;
import com.cashly.cashly_api.invoices.domain.entities.Invoice;
import com.cashly.cashly_api.invoices.domain.repositories.InvoiceRepository;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceStatus;
import com.cashly.cashly_api.payments.domain.entities.Payment;
import com.cashly.cashly_api.payments.domain.repositories.PaymentRepository;
import com.cashly.cashly_api.payments.domain.valueobjects.PaymentId;
import com.cashly.cashly_api.payments.domain.valueobjects.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CreditValidationService implements com.cashly.cashly_api.customers.application.services.CreditValidationService {

    private final CustomerRepository customerRepository;
    private final CustomerBalanceRepository customerBalanceRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentAllocationRepository paymentAllocationRepository;
    private final InvoiceRepository invoiceRepository;
    private final CreditNoteRepository creditNoteRepository;
    private final DebitNoteRepository debitNoteRepository;

    public CreditValidationService(CustomerRepository customerRepository,
                                    CustomerBalanceRepository customerBalanceRepository,
                                    PaymentRepository paymentRepository,
                                    PaymentAllocationRepository paymentAllocationRepository,
                                    InvoiceRepository invoiceRepository,
                                    CreditNoteRepository creditNoteRepository,
                                    DebitNoteRepository debitNoteRepository) {
        this.customerRepository = customerRepository;
        this.customerBalanceRepository = customerBalanceRepository;
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
        BigDecimal pendingBalance = calculatePendingBalance(customerId);
        BigDecimal customerBalance = customerBalanceRepository.findByCustomerId(customerId)
                .map(CustomerBalance::getTotalBalance)
                .orElse(BigDecimal.ZERO);
        BigDecimal availableCredit = creditLimit.add(customerBalance).subtract(pendingBalance)
                .max(BigDecimal.ZERO);

        List<OverdueInvoiceResponse> overdueInvoices = findOverdueInvoices(customerId);

        boolean hasOverdue = !overdueInvoices.isEmpty();
        boolean canCreateCreditInvoice = availableCredit.compareTo(BigDecimal.ZERO) >= 0 && !hasOverdue;

        return new CreditStatusResponse(
                customerId,
                customer.getLegalName().getValue(),
                customer.getRif().getValue(),
                creditLimit,
                pendingBalance,
                customerBalance,
                availableCredit,
                overdueInvoices,
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

            LocalDate dueDate = invoice.getDueDate();

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
        BigDecimal totalApplied = BigDecimal.ZERO;
        for (Invoice invoice : invoices) {
            if (invoice.getStatus() != InvoiceStatus.PENDING_PAYMENT && invoice.getStatus() != InvoiceStatus.PARTIALLY_PAID) {
                continue;
            }
            List<PaymentAllocation> allocations = paymentAllocationRepository.findByInvoiceId(invoice.getId().value());
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

    private List<OverdueInvoiceResponse> findOverdueInvoices(UUID customerId) {
        List<Invoice> allInvoices = invoiceRepository.findAllWithFilters(null, null, customerId, null, null, null, null, "createdAt", "desc");
        List<OverdueInvoiceResponse> result = new ArrayList<>();
        LocalDate now = LocalDate.now(ZoneId.systemDefault());

        for (Invoice invoice : allInvoices) {
            if (invoice.getStatus() == InvoiceStatus.FULLY_RETURNED
                || (invoice.getStatus() != InvoiceStatus.PENDING_PAYMENT && invoice.getStatus() != InvoiceStatus.PARTIALLY_PAID)) {
                continue;
            }

            LocalDate dueDate = invoice.getDueDate();

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

}
