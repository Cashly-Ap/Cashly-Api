package com.cashly.cashly_api.customers.balance.infrastructure.persistence.adapters;

import com.cashly.cashly_api.customers.balance.infrastructure.persistence.mappers.BalanceTransactionJpaMapper;
import com.cashly.cashly_api.customers.balance.infrastructure.persistence.mappers.CustomerBalanceJpaMapper;
import com.cashly.cashly_api.customers.balance.infrastructure.persistence.repositories.CustomerBalanceJpaRepository;

import com.cashly.cashly_api.customers.balance.domain.BalanceTransaction;
import com.cashly.cashly_api.customers.balance.domain.CustomerBalance;
import com.cashly.cashly_api.customers.balance.domain.valueobjects.BalanceType;
import com.cashly.cashly_api.customers.balance.infrastructure.persistence.entities.BalanceTransactionEntity;
import com.cashly.cashly_api.customers.balance.infrastructure.persistence.entities.CustomerBalanceEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerBalanceRepositoryAdapter Unit Tests")
class CustomerBalanceRepositoryAdapterTest {

    @Mock
    private CustomerBalanceJpaRepository jpaRepository;

    @Mock
    private EntityManager entityManager;

    @Captor
    private ArgumentCaptor<CustomerBalanceEntity> balanceEntityCaptor;

    @Captor
    private ArgumentCaptor<BalanceTransactionEntity> transactionEntityCaptor;

    private CustomerBalanceRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new CustomerBalanceRepositoryAdapter(jpaRepository, entityManager);
    }

    @Nested
    @DisplayName("Save")
    class Save {

        @Test
        void should_MapToEntityAndDelegateToJpaRepository() {
            CustomerBalance balance = createTestBalance();

            CustomerBalanceEntity expectedEntity = CustomerBalanceJpaMapper.toEntity(balance);
            when(jpaRepository.save(balanceEntityCaptor.capture())).thenReturn(expectedEntity);

            CustomerBalance saved = adapter.save(balance);

            assertNotNull(saved);
            assertEquals(balance.getId(), saved.getId());
            assertEquals(balance.getCustomerId(), saved.getCustomerId());
            assertEquals(0, balance.getTotalBalance().compareTo(saved.getTotalBalance()));
            assertEquals(balance.getCreatedAt(), saved.getCreatedAt());
            assertEquals(balance.getUpdatedAt(), saved.getUpdatedAt());

            CustomerBalanceEntity captured = balanceEntityCaptor.getValue();
            assertEquals(balance.getId(), captured.getId());
            assertEquals(balance.getCustomerId(), captured.getCustomerId());
            assertEquals(0, balance.getTotalBalance().compareTo(captured.getTotalBalance()));
            assertEquals(balance.getCreatedAt(), captured.getCreatedAt());
            assertEquals(balance.getUpdatedAt(), captured.getUpdatedAt());

            verify(jpaRepository).save(any(CustomerBalanceEntity.class));
        }
    }

    @Nested
    @DisplayName("FindByCustomerId")
    class FindByCustomerId {

        @Test
        void should_ReturnDomainObject_When_EntityFound() {
            CustomerBalance balance = createTestBalance();
            CustomerBalanceEntity entity = CustomerBalanceJpaMapper.toEntity(balance);

            when(jpaRepository.findByCustomerId(balance.getCustomerId())).thenReturn(Optional.of(entity));

            Optional<CustomerBalance> result = adapter.findByCustomerId(balance.getCustomerId());

            assertTrue(result.isPresent());
            assertEquals(balance.getId(), result.get().getId());
            assertEquals(balance.getCustomerId(), result.get().getCustomerId());
            assertEquals(0, balance.getTotalBalance().compareTo(result.get().getTotalBalance()));

            verify(jpaRepository).findByCustomerId(balance.getCustomerId());
        }

        @Test
        void should_ReturnEmpty_When_EntityNotFound() {
            UUID customerId = UUID.randomUUID();
            when(jpaRepository.findByCustomerId(customerId)).thenReturn(Optional.empty());

            Optional<CustomerBalance> result = adapter.findByCustomerId(customerId);

            assertTrue(result.isEmpty());
            verify(jpaRepository).findByCustomerId(customerId);
        }
    }

    @Nested
    @DisplayName("SaveTransaction")
    class SaveTransaction {

        @Test
        void should_PersistEntityAndReturnMappedDomain() {
            BalanceTransaction transaction = createTestTransaction();

            BalanceTransaction result = adapter.saveTransaction(transaction);

            assertNotNull(result);
            assertEquals(transaction.getId(), result.getId());
            assertEquals(transaction.getCustomerId(), result.getCustomerId());
            assertEquals(transaction.getType(), result.getType());
            assertEquals(0, transaction.getAmount().compareTo(result.getAmount()));
            assertEquals(transaction.getReferenceType(), result.getReferenceType());
            assertEquals(transaction.getReferenceId(), result.getReferenceId());
            assertEquals(transaction.getNumber(), result.getNumber());
            assertEquals(transaction.getReferenceNumber(), result.getReferenceNumber());
            assertEquals(transaction.getCreatedBy(), result.getCreatedBy());
            assertEquals(transaction.getCreatedAt(), result.getCreatedAt());

            verify(entityManager).persist(transactionEntityCaptor.capture());
            BalanceTransactionEntity captured = transactionEntityCaptor.getValue();

            assertEquals(transaction.getId(), captured.getId());
            assertEquals(transaction.getCustomerId(), captured.getCustomerId());
            assertEquals(transaction.getType().name(), captured.getType());
            assertEquals(0, transaction.getAmount().compareTo(captured.getAmount()));
            assertEquals(transaction.getReferenceType().name(), captured.getReferenceType());
            assertEquals(transaction.getReferenceId(), captured.getReferenceId());
            assertEquals(transaction.getNumber(), captured.getNumber());
            assertEquals(transaction.getReferenceNumber(), captured.getReferenceNumber());
            assertEquals(transaction.getCreatedBy(), captured.getCreatedBy());
            assertEquals(transaction.getCreatedAt(), captured.getCreatedAt());
        }

        @Test
        void should_HandleNullableFields() {
            BalanceTransaction transaction = BalanceTransaction.reconstitute(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    BalanceType.RETURNS,
                    new BigDecimal("50.00"),
                    BalanceType.REFUND,
                    null,
                    null,
                    null,
                    UUID.randomUUID(),
                    LocalDateTime.now()
            );

            BalanceTransaction result = adapter.saveTransaction(transaction);

            assertNotNull(result);
            assertEquals(transaction.getId(), result.getId());
            assertNull(result.getReferenceId());
            assertNull(result.getNumber());
            assertNull(result.getReferenceNumber());

            verify(entityManager).persist(any(BalanceTransactionEntity.class));
        }
    }

    @Nested
    @DisplayName("FindTransactionsByCustomerId")
    class FindTransactionsByCustomerId {

        @Test
        void should_ReturnPagedTransactions_When_Found() {
            BalanceTransaction transaction = createTestTransaction();
            BalanceTransactionEntity entity = BalanceTransactionJpaMapper.toEntity(transaction);

            Page<BalanceTransactionEntity> entityPage = new PageImpl<>(List.of(entity));

            when(jpaRepository.findTransactionsByCustomerId(any(UUID.class), any(Pageable.class)))
                    .thenReturn(entityPage);

            UUID customerId = UUID.randomUUID();
            Pageable pageable = PageRequest.of(0, 20);
            PageResponse<BalanceTransaction> result = adapter.findTransactionsByCustomerId(customerId, pageable.getPageNumber(), pageable.getPageSize());

            assertNotNull(result);
            assertEquals(1, result.content().size());
            assertEquals(transaction.getId(), result.content().get(0).getId());
            verify(jpaRepository).findTransactionsByCustomerId(customerId, pageable);
        }

        @Test
        void should_ReturnEmptyPage_When_NoTransactions() {
            Page<BalanceTransactionEntity> entityPage = new PageImpl<>(List.of());

            when(jpaRepository.findTransactionsByCustomerId(any(UUID.class), any(Pageable.class)))
                    .thenReturn(entityPage);

            UUID customerId = UUID.randomUUID();
            Pageable pageable = PageRequest.of(0, 20);
            PageResponse<BalanceTransaction> result = adapter.findTransactionsByCustomerId(customerId, pageable.getPageNumber(), pageable.getPageSize());

            assertNotNull(result);
            assertTrue(result.content().isEmpty());
            verify(jpaRepository).findTransactionsByCustomerId(customerId, pageable);
        }
    }

    @Nested
    @DisplayName("CountTransactionsByCustomerId")
    class CountTransactionsByCustomerId {

        @Test
        void should_ReturnCountFromRepository() {
            UUID customerId = UUID.randomUUID();
            when(jpaRepository.countTransactionsByCustomerId(customerId)).thenReturn(5L);

            long count = adapter.countTransactionsByCustomerId(customerId);

            assertEquals(5L, count);
            verify(jpaRepository).countTransactionsByCustomerId(customerId);
        }

        @Test
        void should_ReturnZero_When_NoTransactions() {
            UUID customerId = UUID.randomUUID();
            when(jpaRepository.countTransactionsByCustomerId(customerId)).thenReturn(0L);

            long count = adapter.countTransactionsByCustomerId(customerId);

            assertEquals(0L, count);
            verify(jpaRepository).countTransactionsByCustomerId(customerId);
        }
    }

    private CustomerBalance createTestBalance() {
        return CustomerBalance.reconstitute(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("5000.00"),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private BalanceTransaction createTestTransaction() {
        return BalanceTransaction.reconstitute(
                UUID.randomUUID(),
                UUID.randomUUID(),
                BalanceType.ADVANCED,
                new BigDecimal("150.00"),
                BalanceType.INVOICES,
                UUID.randomUUID(),
                "TX-001",
                "INV-001",
                UUID.randomUUID(),
                LocalDateTime.now()
        );
    }
}
