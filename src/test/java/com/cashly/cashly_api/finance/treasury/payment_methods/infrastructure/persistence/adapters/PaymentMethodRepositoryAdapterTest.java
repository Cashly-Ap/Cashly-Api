package com.cashly.cashly_api.finance.treasury.payment_methods.infrastructure.persistence.adapters;

import com.cashly.cashly_api.finance.treasury.payment_methods.infrastructure.persistence.repositories.PaymentMethodJpaRepository;

import com.cashly.cashly_api.finance.treasury.payment_methods.domain.entity.PaymentMethod;
import com.cashly.cashly_api.finance.treasury.payment_methods.domain.valueobjects.PaymentMethodId;
import com.cashly.cashly_api.finance.treasury.payment_methods.infrastructure.persistence.entities.PaymentMethodEntity;
import com.cashly.cashly_api.finance.treasury.payment_methods.infrastructure.persistence.mappers.PaymentMethodJpaMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentMethodRepositoryAdapter Unit Tests")
class PaymentMethodRepositoryAdapterTest {

    @Mock
    private PaymentMethodJpaRepository jpaRepository;

    @Captor
    private ArgumentCaptor<PaymentMethodEntity> entityCaptor;

    private PaymentMethodRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new PaymentMethodRepositoryAdapter(jpaRepository);
    }

    @Nested
    @DisplayName("Save")
    class Save {

        @Test
        void should_MapToEntityAndDelegateToJpaRepository() {
            PaymentMethod paymentMethod = PaymentMethod.reconstitute(
                    PaymentMethodId.of(UUID.randomUUID()),
                    "PM-001",
                    "Cash",
                    "Cash payment method",
                    true,
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            PaymentMethodEntity expectedEntity = PaymentMethodJpaMapper.toEntity(paymentMethod);
            when(jpaRepository.save(entityCaptor.capture())).thenReturn(expectedEntity);

            PaymentMethod saved = adapter.save(paymentMethod);

            assertNotNull(saved);
            assertEquals(paymentMethod.getId(), saved.getId());
            assertEquals(paymentMethod.getCode(), saved.getCode());
            assertEquals(paymentMethod.getName(), saved.getName());
            assertEquals(paymentMethod.getDescription(), saved.getDescription());
            assertEquals(paymentMethod.isActive(), saved.isActive());
            assertEquals(paymentMethod.getCreatedAt(), saved.getCreatedAt());
            assertEquals(paymentMethod.getUpdatedAt(), saved.getUpdatedAt());

            PaymentMethodEntity captured = entityCaptor.getValue();
            assertEquals(paymentMethod.getId().value(), captured.getId());
            assertEquals(paymentMethod.getCode(), captured.getCode());
            assertEquals(paymentMethod.getName(), captured.getName());
            assertEquals(paymentMethod.getDescription(), captured.getDescription());
            assertEquals(paymentMethod.isActive(), captured.isActive());
            assertEquals(paymentMethod.getCreatedAt(), captured.getCreatedAt());
            assertEquals(paymentMethod.getUpdatedAt(), captured.getUpdatedAt());

            verify(jpaRepository).save(any(PaymentMethodEntity.class));
        }

        @Test
        void should_ReturnUpdatedObject_When_SavingExistingEntity() {
            PaymentMethodId id = PaymentMethodId.of(UUID.randomUUID());
            PaymentMethod existing = PaymentMethod.reconstitute(
                    id, "PM-001", "Cash", "Cash payment method",
                    true, LocalDateTime.now().minusDays(1), LocalDateTime.now().minusDays(1)
            );

            existing.update("Updated Cash", "Updated description", false);

            PaymentMethodEntity expectedEntity = PaymentMethodJpaMapper.toEntity(existing);
            when(jpaRepository.save(any(PaymentMethodEntity.class))).thenReturn(expectedEntity);

            PaymentMethod saved = adapter.save(existing);

            assertNotNull(saved);
            assertEquals(id, saved.getId());
            assertEquals("Updated Cash", saved.getName());
            assertFalse(saved.isActive());

            verify(jpaRepository).save(any(PaymentMethodEntity.class));
        }
    }

    @Nested
    @DisplayName("FindById")
    class FindById {

        @Test
        void should_ReturnDomainObject_When_EntityFound() {
            PaymentMethodId id = PaymentMethodId.generate();
            PaymentMethodEntity entity = PaymentMethodJpaMapper.toEntity(
                    PaymentMethod.reconstitute(
                            id, "PM-001", "Cash", "Cash payment",
                            true, LocalDateTime.now(), LocalDateTime.now()
                    )
            );

            when(jpaRepository.findById(id.value())).thenReturn(Optional.of(entity));

            Optional<PaymentMethod> result = adapter.findById(id);

            assertTrue(result.isPresent());
            assertEquals(id, result.get().getId());
            assertEquals("PM-001", result.get().getCode());
            verify(jpaRepository).findById(id.value());
        }

        @Test
        void should_ReturnEmpty_When_EntityNotFound() {
            PaymentMethodId id = PaymentMethodId.generate();
            when(jpaRepository.findById(id.value())).thenReturn(Optional.empty());

            Optional<PaymentMethod> result = adapter.findById(id);

            assertTrue(result.isEmpty());
            verify(jpaRepository).findById(id.value());
        }
    }

    @Nested
    @DisplayName("FindAll")
    class FindAll {

        @Test
        void should_ReturnDomainObjects_When_EntitiesFound() {
            PaymentMethodEntity entity = PaymentMethodJpaMapper.toEntity(
                    PaymentMethod.reconstitute(
                            PaymentMethodId.generate(), "PM-001", "Cash", "Cash payment",
                            true, LocalDateTime.now(), LocalDateTime.now()
                    )
            );

            when(jpaRepository.findAllActive(true)).thenReturn(List.of(entity));

            List<PaymentMethod> result = adapter.findAll(true);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("PM-001", result.get(0).getCode());
            verify(jpaRepository).findAllActive(true);
        }

        @Test
        void should_ReturnAll_When_ActiveParamIsNull() {
            PaymentMethodEntity entity = PaymentMethodJpaMapper.toEntity(
                    PaymentMethod.reconstitute(
                            PaymentMethodId.generate(), "PM-001", "Cash", "Cash payment",
                            true, LocalDateTime.now(), LocalDateTime.now()
                    )
            );

            when(jpaRepository.findAllActive(null)).thenReturn(List.of(entity));

            List<PaymentMethod> result = adapter.findAll(null);

            assertNotNull(result);
            assertEquals(1, result.size());
            verify(jpaRepository).findAllActive(null);
        }

        @Test
        void should_ReturnEmptyList_When_NoEntitiesFound() {
            when(jpaRepository.findAllActive(false)).thenReturn(List.of());

            List<PaymentMethod> result = adapter.findAll(false);

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(jpaRepository).findAllActive(false);
        }
    }

    @Nested
    @DisplayName("FindByCode")
    class FindByCode {

        @Test
        void should_ReturnDomainObject_When_EntityFound() {
            String code = "PM-001";
            PaymentMethodEntity entity = PaymentMethodJpaMapper.toEntity(
                    PaymentMethod.reconstitute(
                            PaymentMethodId.generate(), code, "Cash", "Cash payment",
                            true, LocalDateTime.now(), LocalDateTime.now()
                    )
            );

            when(jpaRepository.findByCode(code)).thenReturn(Optional.of(entity));

            Optional<PaymentMethod> result = adapter.findByCode(code);

            assertTrue(result.isPresent());
            assertEquals(code, result.get().getCode());
            verify(jpaRepository).findByCode(code);
        }

        @Test
        void should_ReturnEmpty_When_EntityNotFound() {
            String code = "PM-NONEXISTENT";
            when(jpaRepository.findByCode(code)).thenReturn(Optional.empty());

            Optional<PaymentMethod> result = adapter.findByCode(code);

            assertTrue(result.isEmpty());
            verify(jpaRepository).findByCode(code);
        }
    }

    @Nested
    @DisplayName("Delete")
    class Delete {

        @Test
        void should_DelegateToJpaRepository() {
            PaymentMethodId id = PaymentMethodId.generate();

            adapter.delete(id);

            verify(jpaRepository).deleteById(id.value());
        }
    }

}
