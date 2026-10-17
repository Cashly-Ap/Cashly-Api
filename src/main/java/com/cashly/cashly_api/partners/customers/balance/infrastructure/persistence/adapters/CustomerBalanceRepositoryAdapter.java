package com.cashly.cashly_api.partners.customers.balance.infrastructure.persistence.adapters;

import com.cashly.cashly_api.partners.customers.balance.infrastructure.persistence.mappers.BalanceTransactionJpaMapper;
import com.cashly.cashly_api.partners.customers.balance.infrastructure.persistence.mappers.CustomerBalanceJpaMapper;
import com.cashly.cashly_api.partners.customers.balance.infrastructure.persistence.repositories.CustomerBalanceJpaRepository;

import com.cashly.cashly_api.partners.customers.balance.domain.entity.BalanceTransaction;
import com.cashly.cashly_api.partners.customers.balance.domain.entity.CustomerBalance;
import com.cashly.cashly_api.partners.customers.balance.domain.repository.CustomerBalanceRepository;
import com.cashly.cashly_api.partners.customers.balance.infrastructure.persistence.entities.BalanceTransactionEntity;
import jakarta.persistence.EntityManager;
import com.cashly.cashly_api.shared.dto.PageResponse;
import com.cashly.cashly_api.shared.infrastructure.pagination.PageResponseMapper;
import org.springframework.data.domain.PageRequest;
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
    public PageResponse<BalanceTransaction> findTransactionsByCustomerId(UUID customerId, int page, int size) {
        return PageResponseMapper.from(jpaRepository.findTransactionsByCustomerId(customerId, PageRequest.of(page, size))
            .map(BalanceTransactionJpaMapper::toDomain));
    }

    @Override
    public long countTransactionsByCustomerId(UUID customerId) {
        return jpaRepository.countTransactionsByCustomerId(customerId);
    }
}
