package com.cashly.cashly_api.warehouses.infrastructure.adapters.out.persistence.adapters;

import com.cashly.cashly_api.warehouses.infrastructure.adapters.out.persistence.repositories.SpringDataWarehouseStockRepository;

import com.cashly.cashly_api.shared.dto.PageResponse;
import com.cashly.cashly_api.warehouses.domain.entities.WarehouseStock;
import com.cashly.cashly_api.warehouses.infrastructure.adapters.out.persistence.entities.WarehouseStockEntity;
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
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JpaWarehouseStockRepository Unit Tests")
class JpaWarehouseStockRepositoryTest {

    @Mock
    private SpringDataWarehouseStockRepository springDataRepository;

    @Captor
    private ArgumentCaptor<WarehouseStockEntity> entityCaptor;

    private JpaWarehouseStockRepository adapter;

    @BeforeEach
    void setUp() {
        adapter = new JpaWarehouseStockRepository(springDataRepository);
    }

    @Nested
    @DisplayName("Save")
    class Save {

        @Test
        void should_CreateNewStock_When_IdIsNull() {
            UUID warehouseId = UUID.randomUUID();
            UUID productId = UUID.randomUUID();
            WarehouseStock stock = WarehouseStock.create(warehouseId, productId);

            WarehouseStockEntity savedEntity = WarehouseStockEntity.fromDomain(stock);
            when(springDataRepository.save(entityCaptor.capture())).thenReturn(savedEntity);

            WarehouseStock result = adapter.save(stock);

            assertNotNull(result);
            assertEquals(warehouseId, result.getWarehouseId());
            assertEquals(productId, result.getProductId());
            assertEquals(0, result.getQuantity());

            WarehouseStockEntity captured = entityCaptor.getValue();
            assertEquals(warehouseId, captured.getWarehouseId());
            assertEquals(productId, captured.getProductId());

            verify(springDataRepository).save(any(WarehouseStockEntity.class));
        }

        @Test
        void should_UpdateExistingStock_When_IdIsNotNullAndEntityExists() {
            UUID id = UUID.randomUUID();
            UUID warehouseId = UUID.randomUUID();
            UUID productId = UUID.randomUUID();
            WarehouseStock stock = new WarehouseStock(id, warehouseId, productId, 10, 2,
                    LocalDateTime.now(), LocalDateTime.now());

            WarehouseStockEntity existingEntity = WarehouseStockEntity.fromDomain(stock);

            when(springDataRepository.findById(id)).thenReturn(Optional.of(existingEntity));
            when(springDataRepository.save(entityCaptor.capture())).thenReturn(existingEntity);

            WarehouseStock result = adapter.save(stock);

            assertNotNull(result);
            assertEquals(10, result.getQuantity());
            assertEquals(2, result.getReserved());

            WarehouseStockEntity captured = entityCaptor.getValue();
            assertEquals(id, captured.getId());
            assertEquals(10, captured.getQuantity());

            verify(springDataRepository).findById(id);
            verify(springDataRepository).save(any(WarehouseStockEntity.class));
        }

        @Test
        void should_CreateNewStock_When_IdIsNotNullButEntityNotFound() {
            UUID id = UUID.randomUUID();
            UUID warehouseId = UUID.randomUUID();
            UUID productId = UUID.randomUUID();
            WarehouseStock stock = new WarehouseStock(id, warehouseId, productId, 5, 1,
                    LocalDateTime.now(), LocalDateTime.now());

            when(springDataRepository.findById(id)).thenReturn(Optional.empty());

            WarehouseStockEntity savedEntity = WarehouseStockEntity.fromDomain(stock);
            when(springDataRepository.save(entityCaptor.capture())).thenReturn(savedEntity);

            WarehouseStock result = adapter.save(stock);

            assertNotNull(result);
            assertEquals(5, result.getQuantity());
            assertEquals(warehouseId, result.getWarehouseId());
            assertEquals(productId, result.getProductId());

            verify(springDataRepository).findById(id);
            verify(springDataRepository).save(any(WarehouseStockEntity.class));
        }
    }

    @Nested
    @DisplayName("FindByWarehouseIdAndProductId")
    class FindByWarehouseIdAndProductId {

        @Test
        void should_ReturnDomainObject_When_EntityFound() {
            UUID warehouseId = UUID.randomUUID();
            UUID productId = UUID.randomUUID();
            WarehouseStock stock = WarehouseStock.create(warehouseId, productId);
            WarehouseStockEntity entity = WarehouseStockEntity.fromDomain(stock);

            when(springDataRepository.findByWarehouseIdAndProductId(warehouseId, productId))
                    .thenReturn(Optional.of(entity));

            Optional<WarehouseStock> result = adapter.findByWarehouseIdAndProductId(warehouseId, productId);

            assertTrue(result.isPresent());
            assertEquals(warehouseId, result.get().getWarehouseId());
            assertEquals(productId, result.get().getProductId());
            verify(springDataRepository).findByWarehouseIdAndProductId(warehouseId, productId);
        }

        @Test
        void should_ReturnEmpty_When_EntityNotFound() {
            UUID warehouseId = UUID.randomUUID();
            UUID productId = UUID.randomUUID();

            when(springDataRepository.findByWarehouseIdAndProductId(warehouseId, productId))
                    .thenReturn(Optional.empty());

            Optional<WarehouseStock> result = adapter.findByWarehouseIdAndProductId(warehouseId, productId);

            assertTrue(result.isEmpty());
            verify(springDataRepository).findByWarehouseIdAndProductId(warehouseId, productId);
        }
    }

    @Nested
    @DisplayName("FindByWarehouseIdAndProductIdForUpdate")
    class FindByWarehouseIdAndProductIdForUpdate {

        @Test
        void should_ReturnDomainObjectWithPessimisticLock_When_EntityFound() {
            UUID warehouseId = UUID.randomUUID();
            UUID productId = UUID.randomUUID();
            WarehouseStock stock = WarehouseStock.create(warehouseId, productId);
            WarehouseStockEntity entity = WarehouseStockEntity.fromDomain(stock);

            when(springDataRepository.findByWarehouseIdAndProductIdForUpdate(warehouseId, productId))
                    .thenReturn(Optional.of(entity));

            Optional<WarehouseStock> result = adapter.findByWarehouseIdAndProductIdForUpdate(warehouseId, productId);

            assertTrue(result.isPresent());
            assertEquals(warehouseId, result.get().getWarehouseId());
            assertEquals(productId, result.get().getProductId());
            verify(springDataRepository).findByWarehouseIdAndProductIdForUpdate(warehouseId, productId);
        }

        @Test
        void should_ReturnEmpty_When_EntityNotFoundForUpdate() {
            UUID warehouseId = UUID.randomUUID();
            UUID productId = UUID.randomUUID();

            when(springDataRepository.findByWarehouseIdAndProductIdForUpdate(warehouseId, productId))
                    .thenReturn(Optional.empty());

            Optional<WarehouseStock> result = adapter.findByWarehouseIdAndProductIdForUpdate(warehouseId, productId);

            assertTrue(result.isEmpty());
            verify(springDataRepository).findByWarehouseIdAndProductIdForUpdate(warehouseId, productId);
        }
    }

    @Nested
    @DisplayName("FindByWarehouseId")
    class FindByWarehouseId {

        @Test
        void should_ReturnAllStockForWarehouse() {
            UUID warehouseId = UUID.randomUUID();
            WarehouseStock stock = WarehouseStock.create(warehouseId, UUID.randomUUID());
            WarehouseStockEntity entity = WarehouseStockEntity.fromDomain(stock);

            Page<WarehouseStockEntity> page = mock(Page.class);
            when(page.stream()).thenReturn(java.util.stream.Stream.of(entity));
            when(springDataRepository.findByWarehouseId(eq(warehouseId), any(PageRequest.class)))
                    .thenReturn(page);

            List<WarehouseStock> result = adapter.findByWarehouseId(warehouseId);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(warehouseId, result.get(0).getWarehouseId());
            verify(springDataRepository).findByWarehouseId(eq(warehouseId), any(PageRequest.class));
        }

        @Test
        void should_ReturnEmptyList_When_NoStockForWarehouse() {
            UUID warehouseId = UUID.randomUUID();

            Page<WarehouseStockEntity> page = mock(Page.class);
            when(page.stream()).thenReturn(java.util.stream.Stream.of());
            when(springDataRepository.findByWarehouseId(eq(warehouseId), any(PageRequest.class)))
                    .thenReturn(page);

            List<WarehouseStock> result = adapter.findByWarehouseId(warehouseId);

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(springDataRepository).findByWarehouseId(eq(warehouseId), any(PageRequest.class));
        }
    }

    @Nested
    @DisplayName("FindByWarehouseIdPaged")
    class FindByWarehouseIdPaged {

        @SuppressWarnings("unchecked")
        @Test
        void should_ReturnPagedResults_When_EntitiesFound() {
            UUID warehouseId = UUID.randomUUID();
            UUID productId = UUID.randomUUID();
            WarehouseStock stock = WarehouseStock.create(warehouseId, productId);

            Page<WarehouseStockEntity> entityPage = mock(Page.class);
            Page<WarehouseStock> mappedPage = mock(Page.class);

            when(entityPage.map(any(Function.class))).thenReturn(mappedPage);
            when(mappedPage.getContent()).thenReturn(List.of(stock));
            when(mappedPage.getTotalElements()).thenReturn(1L);
            when(mappedPage.getTotalPages()).thenReturn(1);
            when(mappedPage.getSize()).thenReturn(20);
            when(mappedPage.getNumber()).thenReturn(0);
            when(mappedPage.isFirst()).thenReturn(true);
            when(mappedPage.isLast()).thenReturn(true);
            when(mappedPage.isEmpty()).thenReturn(false);

            when(springDataRepository.findByWarehouseId(eq(warehouseId), any(PageRequest.class)))
                    .thenReturn(entityPage);

            PageResponse<WarehouseStock> result = adapter.findByWarehouseIdPaged(warehouseId, 0, 20);

            assertNotNull(result);
            assertEquals(1, result.totalElements());
            assertEquals(1, result.content().size());
            assertEquals(productId, result.content().get(0).getProductId());
            verify(springDataRepository).findByWarehouseId(eq(warehouseId), any(PageRequest.class));
        }

        @SuppressWarnings("unchecked")
        @Test
        void should_ReturnEmpty_When_NoEntitiesFound() {
            UUID warehouseId = UUID.randomUUID();

            Page<WarehouseStockEntity> entityPage = mock(Page.class);
            Page<WarehouseStock> mappedPage = mock(Page.class);

            when(entityPage.map(any(Function.class))).thenReturn(mappedPage);
            when(mappedPage.getContent()).thenReturn(List.of());
            when(mappedPage.getTotalElements()).thenReturn(0L);
            when(mappedPage.getTotalPages()).thenReturn(0);
            when(mappedPage.getSize()).thenReturn(20);
            when(mappedPage.getNumber()).thenReturn(0);
            when(mappedPage.isFirst()).thenReturn(true);
            when(mappedPage.isLast()).thenReturn(true);
            when(mappedPage.isEmpty()).thenReturn(true);

            when(springDataRepository.findByWarehouseId(eq(warehouseId), any(PageRequest.class)))
                    .thenReturn(entityPage);

            PageResponse<WarehouseStock> result = adapter.findByWarehouseIdPaged(warehouseId, 0, 20);

            assertNotNull(result);
            assertTrue(result.empty());
            assertTrue(result.content().isEmpty());
            verify(springDataRepository).findByWarehouseId(eq(warehouseId), any(PageRequest.class));
        }
    }

    @Nested
    @DisplayName("FindByProductId")
    class FindByProductId {

        @Test
        void should_ReturnAllStockForProduct() {
            UUID productId = UUID.randomUUID();
            WarehouseStock stock = WarehouseStock.create(UUID.randomUUID(), productId);
            WarehouseStockEntity entity = WarehouseStockEntity.fromDomain(stock);

            when(springDataRepository.findByProductId(productId)).thenReturn(List.of(entity));

            List<WarehouseStock> result = adapter.findByProductId(productId);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(productId, result.get(0).getProductId());
            verify(springDataRepository).findByProductId(productId);
        }

        @Test
        void should_ReturnEmptyList_When_NoStockForProduct() {
            UUID productId = UUID.randomUUID();

            when(springDataRepository.findByProductId(productId)).thenReturn(List.of());

            List<WarehouseStock> result = adapter.findByProductId(productId);

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(springDataRepository).findByProductId(productId);
        }
    }

    @Nested
    @DisplayName("FindOrCreateByWarehouseIdAndProductIdForUpdate")
    class FindOrCreateByWarehouseIdAndProductIdForUpdate {

        @Test
        void should_insert_missing_row_then_return_it_locked() {
            UUID warehouseId = UUID.randomUUID();
            UUID productId = UUID.randomUUID();
            WarehouseStockEntity entity = WarehouseStockEntity.fromDomain(
                    WarehouseStock.create(warehouseId, productId));
            when(springDataRepository.findByWarehouseIdAndProductIdForUpdate(warehouseId, productId))
                    .thenReturn(java.util.Optional.of(entity));

            WarehouseStock result = adapter.findOrCreateByWarehouseIdAndProductIdForUpdate(warehouseId, productId);

            assertEquals(warehouseId, result.getWarehouseId());
            assertEquals(productId, result.getProductId());
            verify(springDataRepository).insertIfMissing(anyString(), eq(warehouseId.toString()), eq(productId.toString()));
            verify(springDataRepository).findByWarehouseIdAndProductIdForUpdate(warehouseId, productId);
        }
    }

    @Nested
    @DisplayName("ExistsByProductId")
    class ExistsByProductId {

        @Test
        void should_ReturnTrue_When_StockExistsForProduct() {
            UUID productId = UUID.randomUUID();
            when(springDataRepository.existsByProductId(productId)).thenReturn(true);

            boolean result = adapter.existsByProductId(productId);

            assertTrue(result);
            verify(springDataRepository).existsByProductId(productId);
        }

        @Test
        void should_ReturnFalse_When_NoStockForProduct() {
            UUID productId = UUID.randomUUID();
            when(springDataRepository.existsByProductId(productId)).thenReturn(false);

            boolean result = adapter.existsByProductId(productId);

            assertFalse(result);
            verify(springDataRepository).existsByProductId(productId);
        }
    }

    @Nested
    @DisplayName("DeleteByProductId")
    class DeleteByProductId {

        @Test
        void should_DelegateToSpringDataRepository() {
            UUID productId = UUID.randomUUID();

            adapter.deleteByProductId(productId);

            verify(springDataRepository).deleteByProductId(productId);
        }
    }
}
