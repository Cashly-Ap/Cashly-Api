package com.cashly.cashly_api.payments.collection.application.approve.use_case;

import com.cashly.cashly_api.payments.collection.application.ports.out.CashRegisterGateway;
import com.cashly.cashly_api.payments.collection.application.ports.out.PaymentInvoiceGateway;
import com.cashly.cashly_api.payments.collection.application.ports.out.PaymentNotificationGateway;
import com.cashly.cashly_api.payments.collection.application.approve.request.ApprovePaymentRequest;
import com.cashly.cashly_api.payments.collection.application.dto.response.PaymentResponse;
import com.cashly.cashly_api.payments.collection.application.mappers.PaymentMapper;
import com.cashly.cashly_api.payments.collection.domain.entity.Payment;
import com.cashly.cashly_api.payments.collection.domain.repository.PaymentAllocationRepository;
import com.cashly.cashly_api.payments.collection.domain.repository.PaymentRepository;
import com.cashly.cashly_api.payments.collection.domain.entity.PaymentAllocation;
import com.cashly.cashly_api.payments.collection.domain.services.PaymentAllocationService;
import com.cashly.cashly_api.payments.collection.domain.valueobjects.InvoiceRemainingBalance;
import com.cashly.cashly_api.payments.collection.domain.valueobjects.PaymentId;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class ApprovePaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final PaymentAllocationRepository allocationRepository;
    private final PaymentInvoiceGateway invoices;
    private final PaymentNotificationGateway notifications;
    private final CashRegisterGateway cashRegister;
    private final PaymentAllocationService paymentAllocationService;

    public ApprovePaymentUseCase(PaymentRepository paymentRepository,
                                   PaymentAllocationRepository allocationRepository,
                                   PaymentInvoiceGateway invoices,
                                   PaymentNotificationGateway notifications,
                                   CashRegisterGateway cashRegister,
                                   PaymentAllocationService paymentAllocationService) {
        this.paymentRepository = paymentRepository;
        this.allocationRepository = allocationRepository;
        this.invoices = invoices;
        this.notifications = notifications;
        this.cashRegister = cashRegister;
        this.paymentAllocationService = paymentAllocationService;
    }

    @Transactional
    public PaymentResponse execute(UUID id, ApprovePaymentRequest request, UUID userId, String userName) {
        Payment payment = paymentRepository.findById(PaymentId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + id));

        cashRegister.requireOpenRegister();
        payment.approve(request.reason(), userId, userName);

        applyToUnpaidInvoicesOldestFirst(payment);

        Payment saved = paymentRepository.save(payment);
        cashRegister.recordMovement(new CashRegisterGateway.Movement(CashRegisterGateway.Direction.IN,
                CashRegisterGateway.Type.PAYMENT, new CashRegisterGateway.PaymentEntry(saved.getAmount(),
                saved.getPaymentMethodId(), saved.getNotes(), id, userId, userName)));
        notifications.approved(payment.getCustomerId(), id);
        return PaymentMapper.toResponse(saved);
    }

    private void applyToUnpaidInvoicesOldestFirst(Payment payment) {
        List<PaymentInvoiceGateway.InvoiceSnapshot> unpaidInvoices = invoices.findUnpaidByCustomerId(payment.getCustomerId());

        List<InvoiceRemainingBalance> balances = unpaidInvoices.stream()
                .map(invoice -> new InvoiceRemainingBalance(invoice.id(), invoiceRemainingBalance(invoice)))
                .toList();

        List<PaymentAllocation> allocations = paymentAllocationService.allocateOldestFirst(
                payment.getId().value(), balances, payment.getAmount());

        BigDecimal allocatedTotal = BigDecimal.ZERO;
        for (PaymentAllocation allocation : allocations) {
            allocationRepository.save(allocation);
            invoices.recalculateStatus(allocation.getInvoiceId());
            allocatedTotal = allocatedTotal.add(allocation.getAllocatedAmount());
        }

        if (allocatedTotal.compareTo(BigDecimal.ZERO) > 0) {
            payment.apply(allocatedTotal);
        }
    }

    private BigDecimal invoiceRemainingBalance(PaymentInvoiceGateway.InvoiceSnapshot invoice) {
        BigDecimal alreadyAllocated = allocationRepository.findByInvoiceId(invoice.id()).stream()
                .map(PaymentAllocation::getAllocatedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return invoice.total().subtract(alreadyAllocated);
    }
}
