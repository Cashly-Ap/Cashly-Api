package com.parsun.parsun_api.customers.application.mappers;

import com.parsun.parsun_api.customers.application.dto.response.BalanceTransactionResponse;
import com.parsun.parsun_api.customers.application.dto.response.CustomerBalanceResponse;
import com.parsun.parsun_api.customers.domain.entities.BalanceTransaction;
import com.parsun.parsun_api.customers.domain.entities.CustomerBalance;

public final class BalanceMapper {

    private BalanceMapper() {
    }

    public static CustomerBalanceResponse toResponse(CustomerBalance balance) {
        return new CustomerBalanceResponse(
            balance.getCustomerId(),
            balance.getTotalBalance(),
            balance.getCreatedAt(),
            balance.getUpdatedAt()
        );
    }

    public static BalanceTransactionResponse toTransactionResponse(BalanceTransaction transaction) {
        return new BalanceTransactionResponse(
            transaction.getId(),
            transaction.getCustomerId(),
            transaction.getType().name(),
            transaction.getAmount(),
            transaction.getReferenceType().name(),
            transaction.getReferenceId(),
            transaction.getNotes(),
            transaction.getCreatedBy(),
            transaction.getCreatedAt()
        );
    }
}
