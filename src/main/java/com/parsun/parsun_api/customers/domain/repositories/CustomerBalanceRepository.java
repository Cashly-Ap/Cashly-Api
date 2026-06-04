package com.parsun.parsun_api.customers.domain.repositories;

import com.parsun.parsun_api.customers.domain.entities.BalanceTransaction;
import com.parsun.parsun_api.customers.domain.entities.CustomerBalance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface CustomerBalanceRepository {

    CustomerBalance save(CustomerBalance balance);

    Optional<CustomerBalance> findByCustomerId(UUID customerId);

    BalanceTransaction saveTransaction(BalanceTransaction transaction);

    Page<BalanceTransaction> findTransactionsByCustomerId(UUID customerId, Pageable pageable);

    long countTransactionsByCustomerId(UUID customerId);
}
