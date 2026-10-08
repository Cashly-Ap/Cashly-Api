package com.cashly.cashly_api.customers.domain.repositories;

import com.cashly.cashly_api.customers.domain.entities.BalanceTransaction;
import com.cashly.cashly_api.customers.domain.entities.CustomerBalance;
import com.cashly.cashly_api.shared.dto.PageResponse;


import java.util.Optional;
import java.util.UUID;

public interface CustomerBalanceRepository {

    CustomerBalance save(CustomerBalance balance);

    Optional<CustomerBalance> findByCustomerId(UUID customerId);

    BalanceTransaction saveTransaction(BalanceTransaction transaction);

    PageResponse<BalanceTransaction> findTransactionsByCustomerId(UUID customerId, int page, int size);

    long countTransactionsByCustomerId(UUID customerId);
}
