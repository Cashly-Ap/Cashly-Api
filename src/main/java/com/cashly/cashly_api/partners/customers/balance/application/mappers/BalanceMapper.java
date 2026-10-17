package com.cashly.cashly_api.partners.customers.balance.application.mappers;

import com.cashly.cashly_api.partners.customers.balance.application.dto.response.BalanceTransactionResponse;
import com.cashly.cashly_api.partners.customers.balance.application.dto.response.CustomerBalanceResponse;
import com.cashly.cashly_api.partners.customers.balance.domain.entity.BalanceTransaction;
import com.cashly.cashly_api.partners.customers.balance.domain.entity.CustomerBalance;

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
            transaction.getNumber(),
            transaction.getReferenceNumber(),
            transaction.getCreatedBy(),
            transaction.getCreatedAt()
        );
    }
}
