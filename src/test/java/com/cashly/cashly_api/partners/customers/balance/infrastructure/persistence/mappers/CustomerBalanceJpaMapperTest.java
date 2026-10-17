package com.cashly.cashly_api.partners.customers.balance.infrastructure.persistence.mappers;

import com.cashly.cashly_api.partners.customers.balance.domain.entity.CustomerBalance;
import com.cashly.cashly_api.partners.customers.balance.infrastructure.persistence.entities.CustomerBalanceEntity;
import com.cashly.cashly_api.partners.customers.balance.infrastructure.persistence.mappers.CustomerBalanceJpaMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CustomerBalanceJpaMapper Unit Tests")
class CustomerBalanceJpaMapperTest {

    @Test
    void toEntity_Should_MapAllFieldsCorrectly() {
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 15, 10, 30, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 6, 10, 14, 45, 0);

        CustomerBalance domain = CustomerBalance.reconstitute(
                id, customerId, new BigDecimal("5000.00"), createdAt, updatedAt
        );

        CustomerBalanceEntity entity = CustomerBalanceJpaMapper.toEntity(domain);

        assertEquals(id, entity.getId());
        assertEquals(customerId, entity.getCustomerId());
        assertEquals(0, new BigDecimal("5000.00").compareTo(entity.getTotalBalance()));
        assertEquals(createdAt, entity.getCreatedAt());
        assertEquals(updatedAt, entity.getUpdatedAt());
    }

    @Test
    void toEntity_Should_MapZeroBalanceCorrectly() {
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        CustomerBalance domain = CustomerBalance.reconstitute(
                id, customerId, BigDecimal.ZERO, now, now
        );

        CustomerBalanceEntity entity = CustomerBalanceJpaMapper.toEntity(domain);

        assertEquals(id, entity.getId());
        assertEquals(customerId, entity.getCustomerId());
        assertEquals(0, BigDecimal.ZERO.compareTo(entity.getTotalBalance()));
        assertEquals(now, entity.getCreatedAt());
        assertEquals(now, entity.getUpdatedAt());
    }

    @Test
    void toDomain_Should_MapAllFieldsCorrectly() {
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2026, 3, 20, 8, 0, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 6, 15, 16, 30, 0);

        CustomerBalanceEntity entity = new CustomerBalanceEntity();
        entity.setId(id);
        entity.setCustomerId(customerId);
        entity.setTotalBalance(new BigDecimal("2500.00"));
        entity.setCreatedAt(createdAt);
        entity.setUpdatedAt(updatedAt);

        CustomerBalance domain = CustomerBalanceJpaMapper.toDomain(entity);

        assertEquals(id, domain.getId());
        assertEquals(customerId, domain.getCustomerId());
        assertEquals(0, new BigDecimal("2500.00").compareTo(domain.getTotalBalance()));
        assertEquals(createdAt, domain.getCreatedAt());
        assertEquals(updatedAt, domain.getUpdatedAt());
    }

    @Test
    void toDomain_Should_MapNegativeBalanceCorrectly() {
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        CustomerBalanceEntity entity = new CustomerBalanceEntity();
        entity.setId(id);
        entity.setCustomerId(customerId);
        entity.setTotalBalance(new BigDecimal("-150.00"));
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        CustomerBalance domain = CustomerBalanceJpaMapper.toDomain(entity);

        assertEquals(id, domain.getId());
        assertEquals(0, new BigDecimal("-150.00").compareTo(domain.getTotalBalance()));
    }

    @Test
    void roundTrip_Should_PreserveAllValues() {
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 0, 0, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 6, 16, 12, 0, 0);

        CustomerBalance original = CustomerBalance.reconstitute(
                id, customerId, new BigDecimal("10000.00"), createdAt, updatedAt
        );

        CustomerBalanceEntity entity = CustomerBalanceJpaMapper.toEntity(original);
        CustomerBalance restored = CustomerBalanceJpaMapper.toDomain(entity);

        assertEquals(original.getId(), restored.getId());
        assertEquals(original.getCustomerId(), restored.getCustomerId());
        assertEquals(0, original.getTotalBalance().compareTo(restored.getTotalBalance()));
        assertEquals(original.getCreatedAt(), restored.getCreatedAt());
        assertEquals(original.getUpdatedAt(), restored.getUpdatedAt());
    }
}
