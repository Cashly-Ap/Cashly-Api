package com.parsun.parsun_api.customers.application.usecases;

import com.parsun.parsun_api.customers.application.dto.response.BalanceTransactionResponse;
import com.parsun.parsun_api.customers.application.dto.response.CustomerBalanceResponse;
import com.parsun.parsun_api.customers.application.mappers.BalanceMapper;
import com.parsun.parsun_api.customers.domain.entities.BalanceTransaction;
import com.parsun.parsun_api.customers.domain.repositories.CustomerBalanceRepository;
import com.parsun.parsun_api.shared.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
        Page<BalanceTransaction> transactionPage =
            customerBalanceRepository.findTransactionsByCustomerId(customerId, PageRequest.of(page, size));

        var content = transactionPage.getContent().stream()
            .map(BalanceMapper::toTransactionResponse)
            .toList();

        return new PageResponse<>(
            content,
            transactionPage.getTotalElements(),
            transactionPage.getTotalPages(),
            transactionPage.getSize(),
            transactionPage.getNumber(),
            transactionPage.isFirst(),
            transactionPage.isLast(),
            transactionPage.isEmpty()
        );
    }
}
