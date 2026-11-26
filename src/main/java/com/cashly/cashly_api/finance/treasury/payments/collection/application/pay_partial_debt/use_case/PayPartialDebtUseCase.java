package com.cashly.cashly_api.finance.treasury.payments.collection.application.pay_partial_debt.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.finance.treasury.payments.collection.application.ports.out.CashRegisterGateway;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.ports.out.PaymentCustomerGateway;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.ports.out.PaymentInvoiceGateway;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.pay_partial_debt.request.PayPartialDebtRequest;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class PayPartialDebtUseCase {

    private final PaymentRepository paymentRepository;
    private final PaymentAllocationRepository allocationRepository;
    private final PaymentInvoiceGateway invoices;
    private final PaymentCustomerGateway customers;
    private final CashRegisterGateway cashRegister;
    private final PaymentAllocationService paymentAllocationService;

    private final ActivityNotificationRecorder activityNotifications;

    public PayPartialDebtUseCase(PaymentRepository paymentRepository,
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
            PayPartialDebtRequest request, UUID authenticatedUserId,
            String authenticatedUserName) {
        BigDecimal balanceUsed = balanceUsed(request);
        if (balanceUsed.compareTo(request.amount()) > 0) {
            throw new IllegalArgumentException(
                    "Balance amount cannot exceed payment amount");
        }
        BigDecimal paymentAmount = request.amount().subtract(balanceUsed);

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

        List<PaymentInvoiceGateway.InvoiceSnapshot> unpaidInvoices = invoices
                .findUnpaidByCustomerId(customerId);

        Map<UUID, BigDecimal> allocatedByInvoice = unpaidInvoices.isEmpty()
                ? Map.of()
                : allocationRepository.sumByInvoiceIds(unpaidInvoices.stream()
                        .map(PaymentInvoiceGateway.InvoiceSnapshot::id).toList());
        List<InvoiceRemainingBalance> remainingBalances = unpaidInvoices.stream()
                .map(invoice -> new InvoiceRemainingBalance(invoice.id(),
                        invoice.total().subtract(allocatedByInvoice
                                .getOrDefault(invoice.id(), BigDecimal.ZERO))))
                .filter(balance -> balance.remainingAmount()
                        .compareTo(BigDecimal.ZERO) > 0)
                .toList();

        if (remainingBalances.isEmpty()) {
            throw new IllegalArgumentException(
                    "Customer has no unpaid invoices: " + customerId);
        }

        BigDecimal totalRemaining = remainingBalances.stream()
                .map(InvoiceRemainingBalance::remainingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (paymentAmount.compareTo(totalRemaining) > 0) {
            throw new IllegalArgumentException(
                    "Payment amount exceeds customer's outstanding debt");
        }

        if (paymentAmount.compareTo(BigDecimal.ZERO) > 0) {
            cashRegister.requireOpenRegister();
        }
        if (balanceUsed.compareTo(BigDecimal.ZERO) > 0) {
            customers.consumeInvoiceBalance(
                    new PaymentCustomerGateway.BalanceConsumption(customerId,
                            balanceUsed, null, "Pago parcial de deuda",
                            authenticatedUserId));
        }

        Map<UUID, BigDecimal> paidByInvoice = new java.util.LinkedHashMap<>();
        List<CashRegisterGateway.Movement> cashMovements = new ArrayList<>();
        List<PaymentAllocation> newAllocations = new ArrayList<>();
        BigDecimal cashAllocated = applyPlannedPayments(paidByInvoice, cashMovements, newAllocations,
                remainingBalances,
                paymentAmount, request, customerId, authenticatedUserId,
                authenticatedUserName, PaymentSource.PAYMENT_METHOD,
                "Pago parcial de deuda del cliente");
        BigDecimal balanceAllocated = applyPlannedPayments(paidByInvoice, cashMovements, newAllocations,
                subtract(remainingBalances, paymentAmount), balanceUsed, request,
                customerId, authenticatedUserId, authenticatedUserName,
                PaymentSource.CUSTOMER_BALANCE,
                "Pago con saldo a favor - Pago parcial de deuda");

        if (cashAllocated.compareTo(paymentAmount) != 0
                || balanceAllocated.compareTo(balanceUsed) != 0) {
            throw new IllegalStateException(
                    "Payment could not be fully allocated to invoices");
        }

        if (!newAllocations.isEmpty()) {
            allocationRepository.insertAll(newAllocations);
        }
        if (!cashMovements.isEmpty()) {
            cashRegister.recordMovements(cashMovements);
        }
        Map<UUID, BigDecimal> totalsByInvoice = new HashMap<>();
        for (InvoiceRemainingBalance balance : remainingBalances) {
            totalsByInvoice.put(balance.invoiceId(), balance.remainingAmount()
                    .add(allocatedByInvoice.getOrDefault(balance.invoiceId(),
                            BigDecimal.ZERO)));
        }
        Map<String, List<UUID>> byStatus = new java.util.LinkedHashMap<>();
        for (Map.Entry<UUID, BigDecimal> entry : paidByInvoice.entrySet()) {
            BigDecimal total = totalsByInvoice.getOrDefault(entry.getKey(),
                    BigDecimal.ZERO);
            BigDecimal paid = allocatedByInvoice
                    .getOrDefault(entry.getKey(), BigDecimal.ZERO)
                    .add(entry.getValue());
            String status = paid.compareTo(total) >= 0
                    ? "PAID"
                    : paid.signum() > 0 ? "PARTIALLY_PAID"
                            : "PENDING_PAYMENT";
            byStatus.computeIfAbsent(status,
                    key -> new ArrayList<>()).add(entry.getKey());
        }
        byStatus.forEach((status, ids) -> invoices.updateStatuses(ids, status));

        activityNotifications.record(ActivityEvent.of("FINANCE_TREASURY_RECEIVABLES",
                "PARTIAL_PAYMENT", "RECEIVABLE", customerId,
                customerId.toString(), authenticatedUserId));
        return new PaymentResultResponse(null, PaymentStatus.APPLIED.name(),
                "Pago parcial registrado exitosamente", true);
    }

    private BigDecimal balanceUsed(PayPartialDebtRequest request) {
        if (request.useBalance() != null && request.useBalance()
                && request.balanceAmount() != null) {
            return request.balanceAmount();
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal applyPlannedPayments(
            Map<UUID, BigDecimal> paidByInvoice,
            List<CashRegisterGateway.Movement> cashMovements,
            List<PaymentAllocation> newAllocations,
            List<InvoiceRemainingBalance> balances, BigDecimal amount,
            PayPartialDebtRequest request, UUID customerId,
            UUID authenticatedUserId, String authenticatedUserName,
            PaymentSource source, String notes) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        int year = LocalDate.now().getYear();
        int nextSequence = paymentRepository.getNextSequenceForYear(year);
        BigDecimal allocated = BigDecimal.ZERO;

        for (PaymentAllocationService.AllocationPlan plan : paymentAllocationService
                .planOldestFirst(balances, amount)) {
            PaymentNumber paymentNumber = PaymentNumber.of(year,
                    nextSequence++);
            UUID paymentMethodId = source == PaymentSource.CUSTOMER_BALANCE
                    ? null
                    : request.paymentMethodId();

            Payment payment = Payment.create(paymentNumber, customerId,
                    plan.amount(), paymentMethodId, source,
                    request.referenceNumber(), request.paymentDate(), notes,
                    authenticatedUserId, authenticatedUserName,
                    PaymentStatus.APPLIED);

            payment.apply(plan.amount());
            Payment saved = paymentRepository.insert(payment);
            CashRegisterGateway.Movement movement = cashMovement(saved,
                    authenticatedUserId, authenticatedUserName);
            if (movement != null) {
                cashMovements.add(movement);
            }
            newAllocations.add(PaymentAllocation.create(
                    saved.getId().value(), plan.invoiceId(), plan.amount()));
            paidByInvoice.merge(plan.invoiceId(), plan.amount(),
                    BigDecimal::add);
            allocated = allocated.add(plan.amount());
        }

        return allocated;
    }

    private CashRegisterGateway.Movement cashMovement(Payment payment,
            UUID userId, String userName) {
        if (payment.getSource() != PaymentSource.PAYMENT_METHOD) {
            return null;
        }
        return new CashRegisterGateway.Movement(
                CashRegisterGateway.Direction.IN,
                CashRegisterGateway.Type.PAYMENT,
                new CashRegisterGateway.PaymentEntry(payment.getAmount(),
                        payment.getPaymentMethodId(), payment.getNotes(),
                        payment.getId().value(), userId, userName));
    }

    private List<InvoiceRemainingBalance> subtract(
            List<InvoiceRemainingBalance> balances, BigDecimal amount) {
        List<InvoiceRemainingBalance> remaining = new ArrayList<>();
        BigDecimal left = amount;
        for (InvoiceRemainingBalance balance : balances) {
            if (left.compareTo(BigDecimal.ZERO) <= 0) {
                remaining.add(balance);
                continue;
            }
            BigDecimal used = left.min(balance.remainingAmount());
            left = left.subtract(used);
            BigDecimal newRemaining = balance.remainingAmount().subtract(used);
            if (newRemaining.compareTo(BigDecimal.ZERO) > 0) {
                remaining.add(new InvoiceRemainingBalance(balance.invoiceId(),
                        newRemaining));
            }
        }
        return remaining;
    }

}
