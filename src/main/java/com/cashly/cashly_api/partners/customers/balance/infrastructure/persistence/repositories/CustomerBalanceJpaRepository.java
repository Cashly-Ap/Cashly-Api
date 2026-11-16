package com.cashly.cashly_api.partners.customers.balance.infrastructure.persistence.repositories;

import com.cashly.cashly_api.partners.customers.balance.infrastructure.persistence.entities.BalanceTransactionEntity;
import com.cashly.cashly_api.partners.customers.balance.infrastructure.persistence.entities.CustomerBalanceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CustomerBalanceJpaRepository
        extends
            JpaRepository<CustomerBalanceEntity, UUID> {

    Optional<CustomerBalanceEntity> findByCustomerId(UUID customerId);

    @Query("SELECT t FROM BalanceTransactionEntity t WHERE t.customerId = :customerId ORDER BY t.createdAt DESC")
    Page<BalanceTransactionEntity> findTransactionsByCustomerId(
            @Param("customerId") UUID customerId, Pageable pageable);

    @Query("SELECT COUNT(t) FROM BalanceTransactionEntity t WHERE t.customerId = :customerId")
    long countTransactionsByCustomerId(@Param("customerId") UUID customerId);
}
