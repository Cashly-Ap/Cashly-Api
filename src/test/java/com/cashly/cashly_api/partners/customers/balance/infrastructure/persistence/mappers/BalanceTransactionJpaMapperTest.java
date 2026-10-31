package com.cashly.cashly_api.partners.customers.balance.infrastructure.persistence.mappers;

import com.cashly.cashly_api.partners.customers.balance.domain.entity.BalanceTransaction;
import com.cashly.cashly_api.partners.customers.balance.domain.valueobjects.BalanceType;
import com.cashly.cashly_api.partners.customers.balance.infrastructure.persistence.entities.BalanceTransactionEntity;
import com.cashly.cashly_api.partners.customers.balance.infrastructure.persistence.mappers.BalanceTransactionJpaMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BalanceTransactionJpaMapper Unit Tests")
class BalanceTransactionJpaMapperTest {

    @Test
    void toEntity_Should_MapAllFieldsCorrectly() {
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID referenceId = UUID.randomUUID();
        UUID createdBy = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        BalanceTransaction domain = BalanceTransaction.reconstitute(id,
                customerId, BalanceType.ADVANCED, new BigDecimal("150.00"),
                BalanceType.INVOICES, referenceId, "TX-001", "INV-001",
                createdBy, now);

        BalanceTransactionEntity entity = BalanceTransactionJpaMapper
                .toEntity(domain);

        assertEquals(id, entity.getId());
        assertEquals(customerId, entity.getCustomerId());
        assertEquals("ADVANCED", entity.getType());
        assertEquals(0, new BigDecimal("150.00").compareTo(entity.getAmount()));
        assertEquals("INVOICES", entity.getReferenceType());
        assertEquals(referenceId, entity.getReferenceId());
        assertEquals("TX-001", entity.getNumber());
        assertEquals("INV-001", entity.getReferenceNumber());
        assertEquals(createdBy, entity.getCreatedBy());
        assertEquals(now, entity.getCreatedAt());
    }

    @Test
    void toEntity_Should_MapNullableFieldsAsNull() {
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID createdBy = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        BalanceTransaction domain = BalanceTransaction.reconstitute(id,
                customerId, BalanceType.RETURNS, new BigDecimal("75.50"),
                BalanceType.REFUND, null, null, null, createdBy, now);

        BalanceTransactionEntity entity = BalanceTransactionJpaMapper
                .toEntity(domain);

        assertEquals(id, entity.getId());
        assertEquals(customerId, entity.getCustomerId());
        assertEquals("RETURNS", entity.getType());
        assertEquals("REFUND", entity.getReferenceType());
        assertNull(entity.getReferenceId());
        assertNull(entity.getNumber());
        assertNull(entity.getReferenceNumber());
        assertEquals(createdBy, entity.getCreatedBy());
        assertEquals(now, entity.getCreatedAt());
    }

    @Test
    void toDomain_Should_MapAllFieldsCorrectly() {
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID referenceId = UUID.randomUUID();
        UUID createdBy = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        BalanceTransactionEntity entity = new BalanceTransactionEntity();
        entity.setId(id);
        entity.setCustomerId(customerId);
        entity.setType("PAYMENTS");
        entity.setAmount(new BigDecimal("300.00"));
        entity.setReferenceType("ADVANCED");
        entity.setReferenceId(referenceId);
        entity.setNumber("TX-003");
        entity.setReferenceNumber("ADV-001");
        entity.setCreatedBy(createdBy);
        entity.setCreatedAt(now);

        BalanceTransaction domain = BalanceTransactionJpaMapper
                .toDomain(entity);

        assertEquals(id, domain.getId());
        assertEquals(customerId, domain.getCustomerId());
        assertEquals(BalanceType.PAYMENTS, domain.getType());
        assertEquals(0, new BigDecimal("300.00").compareTo(domain.getAmount()));
        assertEquals(BalanceType.ADVANCED, domain.getReferenceType());
        assertEquals(referenceId, domain.getReferenceId());
        assertEquals("TX-003", domain.getNumber());
        assertEquals("ADV-001", domain.getReferenceNumber());
        assertEquals(createdBy, domain.getCreatedBy());
        assertEquals(now, domain.getCreatedAt());
    }

    @Test
    void toDomain_Should_MapAllBalanceTypesCorrectly() {
        for (BalanceType balanceType : BalanceType.values()) {
            UUID id = UUID.randomUUID();
            UUID customerId = UUID.randomUUID();
            UUID createdBy = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();

            BalanceTransactionEntity entity = new BalanceTransactionEntity();
            entity.setId(id);
            entity.setCustomerId(customerId);
            entity.setType(balanceType.name());
            entity.setAmount(BigDecimal.TEN);
            entity.setReferenceType(balanceType.name());
            entity.setCreatedBy(createdBy);
            entity.setCreatedAt(now);

            BalanceTransaction domain = BalanceTransactionJpaMapper
                    .toDomain(entity);

            assertEquals(balanceType, domain.getType());
            assertEquals(balanceType, domain.getReferenceType());
        }
    }

    @Test
    void roundTrip_Should_PreserveAllValues() {
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID referenceId = UUID.randomUUID();
        UUID createdBy = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        BalanceTransaction original = BalanceTransaction.reconstitute(id,
                customerId, BalanceType.CREDIT_NOTE, new BigDecimal("200.00"),
                BalanceType.REFUND, referenceId, "TX-004", "CN-001", createdBy,
                now);

        BalanceTransactionEntity entity = BalanceTransactionJpaMapper
                .toEntity(original);
        BalanceTransaction restored = BalanceTransactionJpaMapper
                .toDomain(entity);

        assertEquals(original.getId(), restored.getId());
        assertEquals(original.getCustomerId(), restored.getCustomerId());
        assertEquals(original.getType(), restored.getType());
        assertEquals(0, original.getAmount().compareTo(restored.getAmount()));
        assertEquals(original.getReferenceType(), restored.getReferenceType());
        assertEquals(original.getReferenceId(), restored.getReferenceId());
        assertEquals(original.getNumber(), restored.getNumber());
        assertEquals(original.getReferenceNumber(),
                restored.getReferenceNumber());
        assertEquals(original.getCreatedBy(), restored.getCreatedBy());
        assertEquals(original.getCreatedAt(), restored.getCreatedAt());
    }
}
