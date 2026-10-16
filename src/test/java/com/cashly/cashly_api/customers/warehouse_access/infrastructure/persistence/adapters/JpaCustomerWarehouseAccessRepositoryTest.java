package com.cashly.cashly_api.customers.warehouse_access.infrastructure.persistence.adapters;

import com.cashly.cashly_api.customers.warehouse_access.infrastructure.persistence.repositories.SpringDataCustomerWarehouseAccessRepository;

import com.cashly.cashly_api.customers.warehouse_access.domain.CustomerWarehouseAccess;
import com.cashly.cashly_api.customers.warehouse_access.infrastructure.persistence.entities.CustomerWarehouseAccessEntity;
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
@DisplayName("JpaCustomerWarehouseAccessRepository Unit Tests")
class JpaCustomerWarehouseAccessRepositoryTest {

    @Mock
    private SpringDataCustomerWarehouseAccessRepository springDataRepository;

    @Captor
    private ArgumentCaptor<CustomerWarehouseAccessEntity> entityCaptor;

    private JpaCustomerWarehouseAccessRepository adapter;

    @BeforeEach
    void setUp() {
        adapter = new JpaCustomerWarehouseAccessRepository(springDataRepository);
    }

    @Nested
    @DisplayName("Save")
    class Save {

        @Test
        void should_CreateEntityFromDomainAndDelegateToSpringDataRepository() {
            CustomerWarehouseAccess access = new CustomerWarehouseAccess(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    LocalDateTime.now(),
                    UUID.randomUUID()
            );

            CustomerWarehouseAccessEntity expectedEntity = CustomerWarehouseAccessEntity.fromDomain(access);
            when(springDataRepository.save(entityCaptor.capture())).thenReturn(expectedEntity);

            CustomerWarehouseAccess saved = adapter.save(access);

            assertNotNull(saved);
            assertEquals(access.getId(), saved.getId());
            assertEquals(access.getCustomerId(), saved.getCustomerId());
            assertEquals(access.getWarehouseId(), saved.getWarehouseId());
            assertEquals(access.getGrantedAt(), saved.getGrantedAt());
            assertEquals(access.getGrantedBy(), saved.getGrantedBy());

            CustomerWarehouseAccessEntity captured = entityCaptor.getValue();
            assertEquals(access.getId(), captured.getId());
            assertEquals(access.getCustomerId(), captured.getCustomerId());
            assertEquals(access.getWarehouseId(), captured.getWarehouseId());
            assertEquals(access.getGrantedAt(), captured.getGrantedAt());
            assertEquals(access.getGrantedBy(), captured.getGrantedBy());

            verify(springDataRepository).save(any(CustomerWarehouseAccessEntity.class));
        }

        @Test
        void should_SaveWithGeneratedId_When_DomainHasNullId() {
            CustomerWarehouseAccess access = CustomerWarehouseAccess.create(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID()
            );

            CustomerWarehouseAccessEntity expectedEntity = CustomerWarehouseAccessEntity.fromDomain(access);
            when(springDataRepository.save(any(CustomerWarehouseAccessEntity.class))).thenReturn(expectedEntity);

            CustomerWarehouseAccess saved = adapter.save(access);

            assertNotNull(saved);
            assertNotNull(saved.getId());
            assertEquals(access.getCustomerId(), saved.getCustomerId());
            assertEquals(access.getWarehouseId(), saved.getWarehouseId());

            verify(springDataRepository).save(any(CustomerWarehouseAccessEntity.class));
        }
    }

    @Nested
    @DisplayName("FindByCustomerIdAndWarehouseId")
    class FindByCustomerIdAndWarehouseId {

        @Test
        void should_ReturnDomainObject_When_EntityFound() {
            UUID customerId = UUID.randomUUID();
            UUID warehouseId = UUID.randomUUID();
            CustomerWarehouseAccessEntity entity = CustomerWarehouseAccessEntity.fromDomain(
                    new CustomerWarehouseAccess(
                            UUID.randomUUID(), customerId, warehouseId,
                            LocalDateTime.now(), UUID.randomUUID()
                    )
            );

            when(springDataRepository.findByCustomerIdAndWarehouseId(customerId, warehouseId))
                    .thenReturn(Optional.of(entity));

            Optional<CustomerWarehouseAccess> result =
                    adapter.findByCustomerIdAndWarehouseId(customerId, warehouseId);

            assertTrue(result.isPresent());
            assertEquals(customerId, result.get().getCustomerId());
            assertEquals(warehouseId, result.get().getWarehouseId());
            verify(springDataRepository).findByCustomerIdAndWarehouseId(customerId, warehouseId);
        }

        @Test
        void should_ReturnEmpty_When_EntityNotFound() {
            UUID customerId = UUID.randomUUID();
            UUID warehouseId = UUID.randomUUID();

            when(springDataRepository.findByCustomerIdAndWarehouseId(customerId, warehouseId))
                    .thenReturn(Optional.empty());

            Optional<CustomerWarehouseAccess> result =
                    adapter.findByCustomerIdAndWarehouseId(customerId, warehouseId);

            assertTrue(result.isEmpty());
            verify(springDataRepository).findByCustomerIdAndWarehouseId(customerId, warehouseId);
        }
    }

    @Nested
    @DisplayName("FindByCustomerId")
    class FindByCustomerId {

        @Test
        void should_ReturnDomainObjects_When_EntitiesFound() {
            UUID customerId = UUID.randomUUID();
            CustomerWarehouseAccessEntity entity = CustomerWarehouseAccessEntity.fromDomain(
                    new CustomerWarehouseAccess(
                            UUID.randomUUID(), customerId, UUID.randomUUID(),
                            LocalDateTime.now(), UUID.randomUUID()
                    )
            );

            when(springDataRepository.findByCustomerId(customerId)).thenReturn(List.of(entity));

            List<CustomerWarehouseAccess> result = adapter.findByCustomerId(customerId);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(customerId, result.get(0).getCustomerId());
            verify(springDataRepository).findByCustomerId(customerId);
        }

        @Test
        void should_ReturnEmptyList_When_NoEntitiesFound() {
            UUID customerId = UUID.randomUUID();
            when(springDataRepository.findByCustomerId(customerId)).thenReturn(List.of());

            List<CustomerWarehouseAccess> result = adapter.findByCustomerId(customerId);

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(springDataRepository).findByCustomerId(customerId);
        }
    }

    @Nested
    @DisplayName("FindByWarehouseId")
    class FindByWarehouseId {

        @Test
        void should_ReturnDomainObjects_When_EntitiesFound() {
            UUID warehouseId = UUID.randomUUID();
            CustomerWarehouseAccessEntity entity = CustomerWarehouseAccessEntity.fromDomain(
                    new CustomerWarehouseAccess(
                            UUID.randomUUID(), UUID.randomUUID(), warehouseId,
                            LocalDateTime.now(), UUID.randomUUID()
                    )
            );

            when(springDataRepository.findByWarehouseId(warehouseId)).thenReturn(List.of(entity));

            List<CustomerWarehouseAccess> result = adapter.findByWarehouseId(warehouseId);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(warehouseId, result.get(0).getWarehouseId());
            verify(springDataRepository).findByWarehouseId(warehouseId);
        }

        @Test
        void should_ReturnEmptyList_When_NoEntitiesFound() {
            UUID warehouseId = UUID.randomUUID();
            when(springDataRepository.findByWarehouseId(warehouseId)).thenReturn(List.of());

            List<CustomerWarehouseAccess> result = adapter.findByWarehouseId(warehouseId);

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(springDataRepository).findByWarehouseId(warehouseId);
        }
    }

    @Nested
    @DisplayName("Delete")
    class Delete {

        @Test
        void should_DelegateToSpringDataRepository() {
            CustomerWarehouseAccess access = new CustomerWarehouseAccess(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    LocalDateTime.now(),
                    UUID.randomUUID()
            );

            adapter.delete(access);

            verify(springDataRepository).deleteById(access.getId());
        }
    }

    @Nested
    @DisplayName("ExistsByCustomerIdAndWarehouseId")
    class ExistsByCustomerIdAndWarehouseId {

        @Test
        void should_ReturnTrue_When_AccessExists() {
            UUID customerId = UUID.randomUUID();
            UUID warehouseId = UUID.randomUUID();

            when(springDataRepository.existsByCustomerIdAndWarehouseId(customerId, warehouseId))
                    .thenReturn(true);

            boolean result = adapter.existsByCustomerIdAndWarehouseId(customerId, warehouseId);

            assertTrue(result);
            verify(springDataRepository).existsByCustomerIdAndWarehouseId(customerId, warehouseId);
        }

        @Test
        void should_ReturnFalse_When_AccessDoesNotExist() {
            UUID customerId = UUID.randomUUID();
            UUID warehouseId = UUID.randomUUID();

            when(springDataRepository.existsByCustomerIdAndWarehouseId(customerId, warehouseId))
                    .thenReturn(false);

            boolean result = adapter.existsByCustomerIdAndWarehouseId(customerId, warehouseId);

            assertFalse(result);
            verify(springDataRepository).existsByCustomerIdAndWarehouseId(customerId, warehouseId);
        }
    }
}
