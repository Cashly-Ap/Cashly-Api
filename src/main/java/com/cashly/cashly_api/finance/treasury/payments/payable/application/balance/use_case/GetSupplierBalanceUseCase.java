package com.cashly.cashly_api.finance.treasury.payments.payable.application.balance.use_case;

import com.cashly.cashly_api.finance.treasury.payments.payable.application.dto.response.SupplierBalanceResponse;
import com.cashly.cashly_api.finance.treasury.payments.payable.application.dto.response.SupplierBalanceTransactionResponse;
import com.cashly.cashly_api.finance.treasury.payments.payable.application.ports.out.SupplierBalanceGateway;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class GetSupplierBalanceUseCase {

    private final SupplierBalanceGateway balances;

    public GetSupplierBalanceUseCase(SupplierBalanceGateway balances) {
        this.balances = balances;
    }

    @Transactional(readOnly = true)
    public SupplierBalanceResponse execute(UUID supplierId) {
        SupplierBalanceGateway.BalanceSnapshot snapshot = balances
                .getSnapshot(supplierId);
        return new SupplierBalanceResponse(snapshot.supplierId(),
                snapshot.totalBalance(), snapshot.createdAt(),
                snapshot.updatedAt());
    }

    @Transactional(readOnly = true)
    public PageResponse<SupplierBalanceTransactionResponse> transactions(
            UUID supplierId, int page, int size) {
        SupplierBalanceGateway.TransactionPage result = balances
                .findTransactions(supplierId, page, size);
        var content = result.content().stream()
                .map(item -> new SupplierBalanceTransactionResponse(item.id(),
                        supplierId, item.type(), item.amount(),
                        item.referenceType(), item.referenceId(), item.number(),
                        item.referenceNumber(), item.createdBy(),
                        item.createdAt()))
                .toList();
        return new PageResponse<>(content, result.totalElements(),
                result.totalPages(), result.size(), result.number(),
                result.number() == 0,
                result.number() >= result.totalPages() - 1, content.isEmpty());
    }
}
