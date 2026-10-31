package com.cashly.cashly_api.finance.treasury.cash_register.sessions.infrastructure.persistence.repositories;

import com.cashly.cashly_api.finance.treasury.cash_register.sessions.infrastructure.persistence.entities.CashMovementJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CashMovementJpaRepository
        extends
            JpaRepository<CashMovementJpaEntity, UUID> {
    List<CashMovementJpaEntity> findByRegisterIdOrderByOccurredAtAsc(
            UUID registerId);
    boolean existsBySourceTypeAndSourceId(String sourceType, UUID sourceId);
}
