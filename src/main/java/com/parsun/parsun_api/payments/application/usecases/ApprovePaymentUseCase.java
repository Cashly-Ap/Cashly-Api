package com.parsun.parsun_api.payments.application.usecases;

import com.parsun.parsun_api.cashregister.application.services.CashMovementService;
import com.parsun.parsun_api.cashregister.domain.valueobjects.CashMovementDirection;
import com.parsun.parsun_api.cashregister.domain.valueobjects.CashMovementType;
import com.parsun.parsun_api.credit_notes.domain.repositories.CreditNoteRepository;
import com.parsun.parsun_api.debit_notes.domain.repositories.DebitNoteRepository;
import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.invoices.domain.services.InvoiceStatusService;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceId;
import com.parsun.parsun_api.notifications.application.services.NotificationService;
import com.parsun.parsun_api.payments.application.dto.request.ApprovePaymentRequest;
import com.parsun.parsun_api.payments.application.dto.response.PaymentResponse;
import com.parsun.parsun_api.payments.application.mappers.PaymentMapper;
import com.parsun.parsun_api.payments.domain.entities.Payment;
import com.parsun.parsun_api.payments.domain.repositories.PaymentAllocationRepository;
import com.parsun.parsun_api.payments.domain.repositories.PaymentRepository;
import com.parsun.parsun_api.payments.domain.entities.PaymentAllocation;
import com.parsun.parsun_api.payments.domain.services.PaymentAllocationService;
import com.parsun.parsun_api.payments.domain.valueobjects.InvoiceRemainingBalance;
import com.parsun.parsun_api.payments.domain.valueobjects.PaymentId;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class ApprovePaymentUseCase {

    private static final List<String> UNPAID_INVOICE_STATUSES = List.of("PENDING_PAYMENT", "PARTIALLY_PAID");

    private final PaymentRepository paymentRepository;
    private final PaymentAllocationRepository allocationRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceStatusService invoiceStatusService;
    private final NotificationService notificationService;
    private final CashMovementService cashMovementService;
    private final CreditNoteRepository creditNoteRepository;
    private final DebitNoteRepository debitNoteRepository;
    private final PaymentAllocationService paymentAllocationService;

    public ApprovePaymentUseCase(PaymentRepository paymentRepository,
                                   PaymentAllocationRepository allocationRepository,
                                   InvoiceRepository invoiceRepository,
                                   InvoiceStatusService invoiceStatusService,
                                   NotificationService notificationService,
                                   CashMovementService cashMovementService,
                                   CreditNoteRepository creditNoteRepository,
                                   DebitNoteRepository debitNoteRepository,
                                   PaymentAllocationService paymentAllocationService) {
        this.paymentRepository = paymentRepository;
        this.allocationRepository = allocationRepository;
        this.invoiceRepository = invoiceRepository;
        this.invoiceStatusService = invoiceStatusService;
        this.notificationService = notificationService;
        this.cashMovementService = cashMovementService;
        this.creditNoteRepository = creditNoteRepository;
        this.debitNoteRepository = debitNoteRepository;
        this.paymentAllocationService = paymentAllocationService;
    }

    @Transactional
    public PaymentResponse execute(UUID id, ApprovePaymentRequest request, UUID userId, String userName) {
        Payment payment = paymentRepository.findById(PaymentId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + id));

        cashMovementService.requireOpenRegister();
        payment.approve(request.reason(), userId, userName);

        if (payment.getTargetInvoiceId() != null) {
            applyToInvoice(payment, payment.getTargetInvoiceId());
        } else {
            applyToUnpaidInvoicesOldestFirst(payment);
        }

        Payment saved = paymentRepository.save(payment);
        cashMovementService.record(
                CashMovementDirection.IN,
                CashMovementType.PAYMENT,
                saved.getAmount(),
                saved.getPaymentMethodId(),
                saved.getNotes(),
                "PAYMENT",
                id,
                userId,
                userName
        );
        notificationService.notifyCustomerPaymentApproved(payment.getCustomerId(), id);
        return PaymentMapper.toResponse(saved);
    }

    private void applyToInvoice(Payment payment, UUID invoiceId) {
        invoiceRepository.findById(new InvoiceId(invoiceId))
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));

        PaymentAllocation allocation = PaymentAllocation.create(
                payment.getId().value(), invoiceId, payment.getAmount());
        allocationRepository.save(allocation);
        payment.apply(payment.getAmount());
        invoiceStatusService.recalculate(invoiceId);
    }

    private void applyToUnpaidInvoicesOldestFirst(Payment payment) {
        List<Invoice> unpaidInvoices = invoiceRepository.findAllWithFilters(
                null, null, payment.getCustomerId(), UNPAID_INVOICE_STATUSES,
                null, null, null, null, null);

        List<InvoiceRemainingBalance> balances = unpaidInvoices.stream()
                .map(invoice -> new InvoiceRemainingBalance(invoice.getId().value(), invoiceRemainingBalance(invoice)))
                .toList();

        List<PaymentAllocation> allocations = paymentAllocationService.allocateOldestFirst(
                payment.getId().value(), balances, payment.getAmount());

        BigDecimal allocatedTotal = BigDecimal.ZERO;
        for (PaymentAllocation allocation : allocations) {
            allocationRepository.save(allocation);
            invoiceStatusService.recalculate(allocation.getInvoiceId());
            allocatedTotal = allocatedTotal.add(allocation.getAllocatedAmount());
        }

        if (allocatedTotal.compareTo(BigDecimal.ZERO) > 0) {
            payment.apply(allocatedTotal);
        }
    }

    private BigDecimal invoiceRemainingBalance(Invoice invoice) {
        BigDecimal creds = creditNoteRepository.findByInvoiceId(invoice.getId().value()).stream()
                .map(c -> c.getAmount() != null ? c.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal debits = debitNoteRepository.findByInvoiceId(invoice.getId().value()).stream()
                .map(d -> d.getAmount() != null ? d.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal invoiceTotal = invoice.calculateTotal(creds, debits);
        BigDecimal alreadyAllocated = allocationRepository.findByInvoiceId(invoice.getId().value()).stream()
                .map(PaymentAllocation::getAllocatedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return invoiceTotal.subtract(alreadyAllocated);
    }
}
