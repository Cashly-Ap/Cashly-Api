package com.cashly.cashly_api.finance.treasury.payments.payable.infrastructure.integration;

import com.cashly.cashly_api.finance.treasury.payments.payable.application.ports.out.SupplierBalanceGateway;
import com.cashly.cashly_api.finance.treasury.payments.payable.infrastructure.persistence.entities.SupplierBalanceEntity;
import com.cashly.cashly_api.finance.treasury.payments.payable.infrastructure.persistence.entities.SupplierBalanceTransactionEntity;
import com.cashly.cashly_api.finance.treasury.payments.payable.infrastructure.persistence.repositories.SpringDataSupplierBalanceRepository;
import com.cashly.cashly_api.finance.treasury.payments.payable.infrastructure.persistence.repositories.SpringDataSupplierBalanceTransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class LocalSupplierBalanceGateway implements SupplierBalanceGateway {

    private final SpringDataSupplierBalanceRepository balances;
    private final SpringDataSupplierBalanceTransactionRepository transactions;

    public LocalSupplierBalanceGateway(
            SpringDataSupplierBalanceRepository balances,
            SpringDataSupplierBalanceTransactionRepository transactions) {
        this.balances = balances;
        this.transactions = transactions;
    }

    @Override
    @Transactional
    public void addCredit(UUID supplierId, BigDecimal amount, UUID referenceId,
            String referenceNumber, UUID createdBy) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        registerTransaction(supplierId, "ADVANCED", "SUPPLIER_PAYMENT",
                referenceId, referenceNumber, amount, createdBy);
    }

    @Override
    @Transactional
    public void subtractCredit(UUID supplierId, BigDecimal amount,
            UUID referenceId, String referenceNumber, UUID createdBy) {
        registerTransaction(supplierId, "REFUND", "SUPPLIER_BALANCE",
                referenceId, referenceNumber, amount.negate(), createdBy);
    }

    @Override
    @Transactional
    public BalanceMutation registerTransaction(UUID supplierId, String type,
            String referenceType, UUID referenceId, String referenceNumber,
            BigDecimal delta, UUID createdBy) {
        if (delta == null || delta.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException(
                    "El monto del movimiento debe ser distinto de cero");
        }
        LocalDateTime now = LocalDateTime.now();
        SupplierBalanceEntity balance = balances
                .findBySupplierId(supplierId).orElseGet(() -> {
                    SupplierBalanceEntity created = new SupplierBalanceEntity();
                    created.setId(UUID.randomUUID());
                    created.setSupplierId(supplierId);
                    created.setTotalBalance(BigDecimal.ZERO);
                    created.setCreatedAt(now);
                    return created;
                });
        BigDecimal newBalance = balance.getTotalBalance().add(delta);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "El monto excede el saldo a favor del proveedor");
        }
        balance.setTotalBalance(newBalance);
        balance.setUpdatedAt(now);
        SupplierBalanceEntity savedBalance = balances.save(balance);

        SupplierBalanceTransactionEntity transaction = new SupplierBalanceTransactionEntity();
        transaction.setId(UUID.randomUUID());
        transaction.setSupplierId(supplierId);
        transaction.setType(type);
        transaction.setAmount(delta.abs());
        transaction.setReferenceType(referenceType);
        transaction.setReferenceId(referenceId);
        transaction.setNumber(referenceNumber);
        transaction.setReferenceNumber(referenceNumber);
        transaction.setCreatedBy(createdBy);
        transaction.setCreatedAt(now);
        SupplierBalanceTransactionEntity savedTransaction = transactions
                .save(transaction);

        return new BalanceMutation(toSnapshot(savedBalance),
                savedTransaction.getId());
    }

    @Override
    public BalanceSnapshot getSnapshot(UUID supplierId) {
        return balances.findBySupplierId(supplierId).map(this::toSnapshot)
                .orElseGet(() -> new BalanceSnapshot(supplierId,
                        BigDecimal.ZERO, null, null));
    }

    @Override
    public TransactionPage findTransactions(UUID supplierId, int page,
            int size) {
        Page<SupplierBalanceTransactionEntity> result = transactions
                .findBySupplierIdOrderByCreatedAtDesc(supplierId, PageRequest
                        .of(page, size, Sort.by("createdAt").descending()));
        List<TransactionView> content = result.getContent().stream()
                .map(this::toView).toList();
        return new TransactionPage(content, result.getTotalElements(),
                result.getTotalPages(), result.getSize(), result.getNumber());
    }

    @Override
    public BigDecimal getBalance(UUID supplierId) {
        return balances.findBySupplierId(supplierId)
                .map(SupplierBalanceEntity::getTotalBalance)
                .orElse(BigDecimal.ZERO);
    }

    private BalanceSnapshot toSnapshot(SupplierBalanceEntity entity) {
        return new BalanceSnapshot(entity.getSupplierId(),
                entity.getTotalBalance(), entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    private TransactionView toView(SupplierBalanceTransactionEntity entity) {
        return new TransactionView(entity.getId(), entity.getType(),
                entity.getAmount(), entity.getReferenceType(),
                entity.getReferenceId(), entity.getNumber(),
                entity.getReferenceNumber(), entity.getCreatedBy(),
                entity.getCreatedAt());
    }
}
