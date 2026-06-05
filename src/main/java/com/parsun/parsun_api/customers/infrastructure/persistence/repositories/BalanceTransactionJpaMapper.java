package com.parsun.parsun_api.customers.infrastructure.persistence.repositories;

import com.parsun.parsun_api.customers.domain.entities.BalanceTransaction;
import com.parsun.parsun_api.customers.infrastructure.persistence.entities.BalanceTransactionEntity;

public final class BalanceTransactionJpaMapper {

    private BalanceTransactionJpaMapper() {
    }

    public static BalanceTransactionEntity toEntity(BalanceTransaction domain) {
        BalanceTransactionEntity entity = new BalanceTransactionEntity();
        entity.setId(domain.getId());
        entity.setCustomerId(domain.getCustomerId());
        entity.setType(domain.getType().name());
        entity.setAmount(domain.getAmount());
        entity.setReferenceType(domain.getReferenceType().name());
        entity.setReferenceId(domain.getReferenceId());
        entity.setNumber(domain.getNumber());
        entity.setReferenceNumber(domain.getReferenceNumber());
        entity.setCreatedBy(domain.getCreatedBy());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }

    @SuppressWarnings("unused")
    public static BalanceTransaction toDomain(BalanceTransactionEntity entity) {
        return BalanceTransaction.reconstitute(
            entity.getId(),
            entity.getCustomerId(),
            com.parsun.parsun_api.customers.domain.valueobjects.BalanceType.valueOf(entity.getType()),
            entity.getAmount(),
            com.parsun.parsun_api.customers.domain.valueobjects.BalanceType.valueOf(entity.getReferenceType()),
            entity.getReferenceId(),
            entity.getNumber(),
            entity.getReferenceNumber(),
            entity.getCreatedBy(),
            entity.getCreatedAt()
        );
    }
}
