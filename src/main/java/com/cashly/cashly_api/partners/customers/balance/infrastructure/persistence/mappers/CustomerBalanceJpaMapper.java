package com.cashly.cashly_api.partners.customers.balance.infrastructure.persistence.mappers;

import com.cashly.cashly_api.partners.customers.balance.domain.entity.CustomerBalance;
import com.cashly.cashly_api.partners.customers.balance.infrastructure.persistence.entities.CustomerBalanceEntity;

import java.util.UUID;

public final class CustomerBalanceJpaMapper {

    private CustomerBalanceJpaMapper() {
    }

    public static CustomerBalanceEntity toEntity(CustomerBalance domain) {
        CustomerBalanceEntity entity = new CustomerBalanceEntity();
        entity.setId(domain.getId());
        entity.setCustomerId(domain.getCustomerId());
        entity.setTotalBalance(domain.getTotalBalance());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }

    public static CustomerBalance toDomain(CustomerBalanceEntity entity) {
        return CustomerBalance.reconstitute(entity.getId(),
                entity.getCustomerId(), entity.getTotalBalance(),
                entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
