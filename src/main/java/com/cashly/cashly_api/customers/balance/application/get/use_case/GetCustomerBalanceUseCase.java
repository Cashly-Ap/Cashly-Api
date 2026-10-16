package com.cashly.cashly_api.customers.balance.application.get.use_case;

import com.cashly.cashly_api.customers.balance.application.dto.response.BalanceTransactionResponse;
import com.cashly.cashly_api.customers.balance.application.dto.response.CustomerBalanceResponse;
import com.cashly.cashly_api.customers.balance.application.mappers.BalanceMapper;
import com.cashly.cashly_api.customers.balance.domain.entity.BalanceTransaction;
import com.cashly.cashly_api.customers.balance.domain.repository.CustomerBalanceRepository;
import com.cashly.cashly_api.shared.dto.PageResponse;


import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

public class GetCustomerBalanceUseCase {

    private final CustomerBalanceRepository customerBalanceRepository;

    public GetCustomerBalanceUseCase(CustomerBalanceRepository customerBalanceRepository) {
        this.customerBalanceRepository = customerBalanceRepository;
    }

    @Transactional(readOnly = true)
    public CustomerBalanceResponse execute(UUID customerId) {
        return customerBalanceRepository.findByCustomerId(customerId)
            .map(BalanceMapper::toResponse)
            .orElseGet(() -> new CustomerBalanceResponse(
                customerId,
                BigDecimal.ZERO,
                null,
                null
            ));
    }

    @Transactional(readOnly = true)
    public PageResponse<BalanceTransactionResponse> getTransactions(UUID customerId, int page, int size) {
        PageResponse<BalanceTransaction> transactionPage =
            customerBalanceRepository.findTransactionsByCustomerId(customerId, page, size);

        var content = transactionPage.content().stream()
            .map(BalanceMapper::toTransactionResponse)
            .toList();

        return new PageResponse<>(
            content,
            transactionPage.totalElements(),
            transactionPage.totalPages(),
            transactionPage.size(),
            transactionPage.number(),
            transactionPage.first(),
            transactionPage.last(),
            transactionPage.empty()
        );
    }
}
