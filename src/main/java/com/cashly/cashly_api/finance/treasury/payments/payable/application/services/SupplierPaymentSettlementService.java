package com.cashly.cashly_api.finance.treasury.payments.payable.application.services;

import com.cashly.cashly_api.finance.treasury.payments.payable.application.ports.out.PayablePurchaseGateway;
import com.cashly.cashly_api.finance.treasury.payments.payable.application.ports.out.SupplierBalanceGateway;
import com.cashly.cashly_api.finance.treasury.payments.payable.application.ports.out.SupplierPaymentCashRegisterGateway;
import com.cashly.cashly_api.finance.treasury.payments.payable.domain.entity.SupplierPayment;
import com.cashly.cashly_api.finance.treasury.payments.payable.domain.entity.SupplierPaymentAllocation;
import com.cashly.cashly_api.finance.treasury.payments.payable.domain.repository.SupplierPaymentAllocationRepository;
import com.cashly.cashly_api.finance.treasury.payments.payable.domain.repository.SupplierPaymentRepository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class SupplierPaymentSettlementService {

    private final PayablePurchaseGateway purchaseGateway;
    private final SupplierPaymentAllocationRepository allocations;
    private final SupplierPaymentRepository payments;
    private final SupplierPaymentCashRegisterGateway cashRegister;
    private final SupplierBalanceGateway balances;

    public SupplierPaymentSettlementService(
            PayablePurchaseGateway purchaseGateway,
            SupplierPaymentAllocationRepository allocations,
            SupplierPaymentRepository payments,
            SupplierPaymentCashRegisterGateway cashRegister,
            SupplierBalanceGateway balances) {
        this.purchaseGateway = purchaseGateway;
        this.allocations = allocations;
        this.payments = payments;
        this.cashRegister = cashRegister;
        this.balances = balances;
    }

    @Transactional
    public SupplierPayment settle(SupplierPayment payment, UUID actorId,
            String actorName) {
        BigDecimal remaining = payment.getAmount();
        List<PayablePurchaseGateway.PayablePurchase> payable = purchaseGateway
                .findPayableBySupplier(payment.getSupplierId());
        for (PayablePurchaseGateway.PayablePurchase purchase : payable) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            BigDecimal alreadyPaid = allocations
                    .getAllocatedAmountByPurchaseId(purchase.id());
            BigDecimal balance = purchase.balanceTotal()
                    .subtract(alreadyPaid);
            if (balance.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            BigDecimal toApply = remaining.min(balance);
            allocations.save(SupplierPaymentAllocation.create(
                    payment.getId().value(), purchase.id(), toApply));
            purchaseGateway.applyPaidTotal(purchase.id(),
                    alreadyPaid.add(toApply));
            remaining = remaining.subtract(toApply);
        }
        cashRegister.recordOutgoing(payment.getPaymentDate(),
                payment.getAmount(), payment.getPaymentMethodId(),
                "Pago a proveedor " + payment.getPaymentNumber().value(),
                payment.getId().value(), actorId, actorName);
        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            balances.addCredit(payment.getSupplierId(), remaining,
                    payment.getId().value(),
                    payment.getPaymentNumber().value(), actorId);
        }
        BigDecimal applied = payment.getAmount().subtract(remaining);
        if (applied.compareTo(BigDecimal.ZERO) > 0) {
            payment.apply(applied);
        }
        return payments.save(payment);
    }

    @Transactional
    public SupplierPayment settlePurchase(SupplierPayment payment,
            UUID purchaseId, BigDecimal amount, UUID actorId,
            String actorName) {
        PayablePurchaseGateway.PayablePurchase purchase = purchaseGateway
                .findById(purchaseId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Purchase not found: " + purchaseId));
        BigDecimal alreadyPaid = allocations
                .getAllocatedAmountByPurchaseId(purchaseId);
        BigDecimal balance = purchase.balanceTotal().subtract(alreadyPaid);
        if (amount.compareTo(balance) > 0) {
            throw new IllegalArgumentException(
                    "Amount exceeds purchase balance: " + balance);
        }
        allocations.save(SupplierPaymentAllocation.create(
                payment.getId().value(), purchaseId, amount));
        purchaseGateway.applyPaidTotal(purchaseId, alreadyPaid.add(amount));
        cashRegister.recordOutgoing(payment.getPaymentDate(), amount,
                payment.getPaymentMethodId(),
                "Pago a proveedor " + payment.getPaymentNumber().value(),
                payment.getId().value(), actorId, actorName);
        BigDecimal remaining = payment.getAmount().subtract(amount);
        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            balances.addCredit(payment.getSupplierId(), remaining,
                    payment.getId().value(),
                    payment.getPaymentNumber().value(), actorId);
        }
        payment.apply(amount);
        return payments.save(payment);
    }
}
