package com.cashly.cashly_api.finance.treasury.payments.payable.application.ports.out;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface SupplierBalanceGateway {

    void addCredit(UUID supplierId, BigDecimal amount, UUID referenceId,
            String referenceNumber, UUID createdBy);

    void subtractCredit(UUID supplierId, BigDecimal amount, UUID referenceId,
            String referenceNumber, UUID createdBy);

    BalanceMutation registerTransaction(UUID supplierId, String type,
            String referenceType, UUID referenceId, String referenceNumber,
            BigDecimal delta, UUID createdBy);

    BalanceSnapshot getSnapshot(UUID supplierId);

    TransactionPage findTransactions(UUID supplierId, int page, int size);

    BigDecimal getBalance(UUID supplierId);

    record BalanceSnapshot(UUID supplierId, BigDecimal totalBalance,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
    }

    record BalanceMutation(BalanceSnapshot balance, UUID transactionId) {
    }

    record TransactionView(UUID id, String type, BigDecimal amount,
            String referenceType, UUID referenceId, String number,
            String referenceNumber, UUID createdBy, LocalDateTime createdAt) {
    }

    record TransactionPage(List<TransactionView> content, long totalElements,
            int totalPages, int size, int number) {
    }
}
