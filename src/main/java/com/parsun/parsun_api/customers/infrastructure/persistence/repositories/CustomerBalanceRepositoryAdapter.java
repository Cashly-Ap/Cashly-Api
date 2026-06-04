package com.parsun.parsun_api.customers.infrastructure.persistence.repositories;

import com.parsun.parsun_api.customers.domain.entities.BalanceTransaction;
import com.parsun.parsun_api.customers.domain.entities.CustomerBalance;
import com.parsun.parsun_api.customers.domain.repositories.CustomerBalanceRepository;
import com.parsun.parsun_api.customers.infrastructure.persistence.entities.BalanceTransactionEntity;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
public class CustomerBalanceRepositoryAdapter implements CustomerBalanceRepository {

    private final CustomerBalanceJpaRepository jpaRepository;
    private final EntityManager entityManager;

    public CustomerBalanceRepositoryAdapter(CustomerBalanceJpaRepository jpaRepository,
                                             EntityManager entityManager) {
        this.jpaRepository = jpaRepository;
        this.entityManager = entityManager;
    }

    @Override
    public CustomerBalance save(CustomerBalance balance) {
        var entity = CustomerBalanceJpaMapper.toEntity(balance);
        var saved = jpaRepository.save(entity);
        return CustomerBalanceJpaMapper.toDomain(saved);
    }

    @Override
    public Optional<CustomerBalance> findByCustomerId(UUID customerId) {
        return jpaRepository.findByCustomerId(customerId)
            .map(CustomerBalanceJpaMapper::toDomain);
    }

    @Override
    @Transactional
    public BalanceTransaction saveTransaction(BalanceTransaction transaction) {
        var entity = BalanceTransactionJpaMapper.toEntity(transaction);
        entityManager.persist(entity);
        return BalanceTransactionJpaMapper.toDomain(entity);
    }

    @Override
    public Page<BalanceTransaction> findTransactionsByCustomerId(UUID customerId, Pageable pageable) {
        return jpaRepository.findTransactionsByCustomerId(customerId, pageable)
            .map(BalanceTransactionJpaMapper::toDomain);
    }

    @Override
    public long countTransactionsByCustomerId(UUID customerId) {
        return jpaRepository.countTransactionsByCustomerId(customerId);
    }
}
