package com.cashly.cashly_api.finance.treasury.payments.collection.application.pay_customer_debt.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.finance.treasury.payments.collection.application.ports.out.CashRegisterGateway;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.ports.out.PaymentCustomerGateway;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.ports.out.PaymentInvoiceGateway;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.pay_customer_debt.request.PayCustomerDebtRequest;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.dto.response.PaymentResultResponse;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.entity.PaymentAllocation;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentAllocationRepository;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.entity.Payment;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentRepository;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.services.PaymentAllocationService;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.InvoiceRemainingBalance;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.PaymentNumber;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.PaymentSource;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.PaymentStatus;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class PayAllCustomerDebtUseCase {

    private final PaymentRepository paymentRepository;
    private final PaymentAllocationRepository allocationRepository;
    private final PaymentInvoiceGateway invoices;
    private final PaymentCustomerGateway customers;
    private final CashRegisterGateway cashRegister;
    private final PaymentAllocationService paymentAllocationService;

    private final ActivityNotificationRecorder activityNotifications;

    public PayAllCustomerDebtUseCase(PaymentRepository paymentRepository,
            PaymentAllocationRepository allocationRepository,
            PaymentInvoiceGateway invoices, PaymentCustomerGateway customers,
            CashRegisterGateway cashRegister,
            PaymentAllocationService paymentAllocationService,
            ActivityNotificationRecorder activityNotifications) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.paymentRepository = paymentRepository;
        this.allocationRepository = allocationRepository;
        this.invoices = invoices;
        this.customers = customers;
        this.cashRegister = cashRegister;
        this.paymentAllocationService = paymentAllocationService;
    }

    @Transactional
    public PaymentResultResponse execute(UUID customerId,
            PayCustomerDebtRequest request, UUID authenticatedUserId,
            String authenticatedUserName) {
        List<PaymentInvoiceGateway.InvoiceSnapshot> unpaidInvoices = invoices
                .findUnpaidByCustomerId(customerId);

        if (unpaidInvoices.isEmpty()) {
            throw new IllegalArgumentException(
                    "Customer has no unpaid invoices: " + customerId);
        }

        BigDecimal totalAmount = unpaidInvoices.stream()
                .map(PaymentInvoiceGateway.InvoiceSnapshot::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal balanceUsed = BigDecimal.ZERO;
        if (request.useBalance() != null && request.useBalance()
                && request.balanceAmount() != null) {
            balanceUsed = request.balanceAmount();
        }

        BigDecimal paymentAmount = totalAmount.subtract(balanceUsed);

        if (paymentAmount.compareTo(BigDecimal.ZERO) <= 0
                && balanceUsed.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Payment amount must be greater than zero");
        }

        if (paymentAmount.compareTo(BigDecimal.ZERO) > 0
                && request.paymentMethodId() == null) {
            throw new IllegalArgumentException(
                    "Payment method is required when payment amount is greater than zero");
        }
        if (paymentAmount.compareTo(BigDecimal.ZERO) > 0)
            cashRegister.requireOpenRegister();
        if (balanceUsed.compareTo(BigDecimal.ZERO) > 0) {
            customers.consumeInvoiceBalance(
                    new PaymentCustomerGateway.BalanceConsumption(customerId,
                            balanceUsed, null, "Pago total de deuda",
                            authenticatedUserId));
        }

        int year = LocalDate.now().getYear();

        if (paymentAmount.compareTo(BigDecimal.ZERO) > 0) {
            int sequence = paymentRepository.getNextSequenceForYear(year);
            PaymentNumber paymentNumber = PaymentNumber.of(year, sequence);

            Payment payment = Payment.create(paymentNumber, customerId,
                    paymentAmount, request.paymentMethodId(),
                    request.referenceNumber(), request.paymentDate(),
                    "Pago total de deuda del cliente", authenticatedUserId,
                    authenticatedUserName, PaymentStatus.APPLIED);

            Payment saved = paymentRepository.save(payment);
            cashRegister.recordMovement(new CashRegisterGateway.Movement(
                    CashRegisterGateway.Direction.IN,
                    CashRegisterGateway.Type.PAYMENT,
                    new CashRegisterGateway.PaymentEntry(saved.getAmount(),
                            saved.getPaymentMethodId(), saved.getNotes(),
                            saved.getId().value(), authenticatedUserId,
                            authenticatedUserName)));

            List<InvoiceRemainingBalance> grossBalances = unpaidInvoices
                    .stream()
                    .map(invoice -> new InvoiceRemainingBalance(invoice.id(),
                            invoice.total()))
                    .toList();
            List<PaymentAllocation> allocations = paymentAllocationService
                    .allocateOldestFirst(saved.getId().value(), grossBalances,
                            totalAmount);
            allocations.forEach(allocationRepository::save);

            payment.apply(paymentAmount);
            paymentRepository.save(payment);
        }

        if (balanceUsed.compareTo(BigDecimal.ZERO) > 0) {
            int balanceSequence = paymentRepository
                    .getNextSequenceForYear(year);
            PaymentNumber balancePaymentNumber = PaymentNumber.of(year,
                    balanceSequence);

            Payment balancePayment = Payment.create(balancePaymentNumber,
                    customerId, balanceUsed, null,
                    PaymentSource.CUSTOMER_BALANCE, null, request.paymentDate(),
                    "Pago con saldo a favor - Pago total de deuda",
                    authenticatedUserId, authenticatedUserName,
                    PaymentStatus.APPLIED);

            Payment savedBalancePayment = paymentRepository
                    .save(balancePayment);

            List<InvoiceRemainingBalance> netBalances = unpaidInvoices.stream()
                    .map(invoice -> new InvoiceRemainingBalance(invoice.id(),
                            invoiceRemainingAfterAllocations(invoice)))
                    .toList();
            List<PaymentAllocation> balanceAllocations = paymentAllocationService
                    .allocateOldestFirst(savedBalancePayment.getId().value(),
                            netBalances, balanceUsed);
            balanceAllocations.forEach(allocationRepository::save);

            balancePayment.apply(balanceUsed);
            paymentRepository.save(balancePayment);
        }

        for (PaymentInvoiceGateway.InvoiceSnapshot invoice : unpaidInvoices) {
            invoices.recalculateStatus(invoice.id());
        }

        activityNotifications.record(ActivityEvent.of("FINANCE_TREASURY_RECEIVABLES",
                "PAY_ALL", "RECEIVABLE", customerId,
                customerId.toString(), authenticatedUserId));
        return new PaymentResultResponse(null, PaymentStatus.APPLIED.name(),
                "Pago total registrado exitosamente", true);
    }

    private BigDecimal invoiceRemainingAfterAllocations(
            PaymentInvoiceGateway.InvoiceSnapshot invoice) {
        BigDecimal alreadyAllocated = allocationRepository
                .findByInvoiceId(invoice.id()).stream()
                .map(PaymentAllocation::getAllocatedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return invoice.total().subtract(alreadyAllocated);
    }
}
