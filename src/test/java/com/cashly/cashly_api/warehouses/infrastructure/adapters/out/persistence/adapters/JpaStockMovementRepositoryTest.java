package com.cashly.cashly_api.warehouses.infrastructure.adapters.out.persistence.adapters;

import com.cashly.cashly_api.warehouses.infrastructure.adapters.out.persistence.repositories.SpringDataStockMovementRepository;

import com.cashly.cashly_api.shared.dto.PageResponse;
import com.cashly.cashly_api.warehouses.domain.entities.MovementType;
import com.cashly.cashly_api.warehouses.domain.entities.ReferenceType;
import com.cashly.cashly_api.warehouses.domain.entities.StockMovement;
import com.cashly.cashly_api.warehouses.infrastructure.adapters.out.persistence.entities.StockMovementEntity;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JpaStockMovementRepository Unit Tests")
class JpaStockMovementRepositoryTest {

    @Mock
    private SpringDataStockMovementRepository springDataRepository;

    @Captor
    private ArgumentCaptor<StockMovementEntity> entityCaptor;

    private JpaStockMovementRepository adapter;

    @BeforeEach
    void setUp() {
        adapter = new JpaStockMovementRepository(springDataRepository);
    }

    private StockMovement createSampleMovement() {
        return new StockMovement(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                MovementType.ENTRY,
                10,
                100,
                110,
                null,
                null,
                null,
                null,
                null,
                "Initial stock entry",
                LocalDateTime.now()
        );
    }

    @Nested
    @DisplayName("Save")
    class Save {

        @Test
        void should_CreateEntityFromDomainAndDelegateToSpringDataRepository() {
            StockMovement movement = createSampleMovement();

            StockMovementEntity expectedEntity = StockMovementEntity.fromDomain(movement);
            when(springDataRepository.save(entityCaptor.capture())).thenReturn(expectedEntity);

            StockMovement saved = adapter.save(movement);

            assertNotNull(saved);
            assertEquals(movement.getId(), saved.getId());
            assertEquals(movement.getWarehouseId(), saved.getWarehouseId());
            assertEquals(movement.getProductId(), saved.getProductId());
            assertEquals(movement.getMovementType(), saved.getMovementType());
            assertEquals(movement.getQuantity(), saved.getQuantity());
            assertEquals(movement.getPreviousStock(), saved.getPreviousStock());
            assertEquals(movement.getNewStock(), saved.getNewStock());
            assertEquals(movement.getNotes(), saved.getNotes());

            StockMovementEntity captured = entityCaptor.getValue();
            assertEquals(movement.getId(), captured.getId());
            assertEquals(movement.getWarehouseId(), captured.getWarehouseId());
            assertEquals(movement.getProductId(), captured.getProductId());
            assertEquals(movement.getMovementType(), captured.getMovementType());
            assertEquals(movement.getQuantity(), captured.getQuantity());
            assertEquals(movement.getPreviousStock(), captured.getPreviousStock());
            assertEquals(movement.getNewStock(), captured.getNewStock());
            assertEquals(movement.getNotes(), captured.getNotes());
            assertEquals(movement.getCreatedAt(), captured.getCreatedAt());

            verify(springDataRepository).save(any(StockMovementEntity.class));
        }

        @Test
        void should_SaveWithGeneratedId_When_DomainHasNullId() {
            StockMovement movement = StockMovement.create(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    MovementType.EXIT,
                    5,
                    110,
                    105,
                    null,
                    null,
                    "Sale exit"
            );

            StockMovementEntity expectedEntity = StockMovementEntity.fromDomain(movement);
            when(springDataRepository.save(any(StockMovementEntity.class))).thenReturn(expectedEntity);

            StockMovement saved = adapter.save(movement);

            assertNotNull(saved);
            assertNotNull(saved.getId());
            assertEquals(movement.getWarehouseId(), saved.getWarehouseId());
            assertEquals(movement.getProductId(), saved.getProductId());

            verify(springDataRepository).save(any(StockMovementEntity.class));
        }
    }

    @Nested
    @DisplayName("FindByWarehouseIdAndProductId")
    class FindByWarehouseIdAndProductId {

        @Test
        void should_ReturnPageResponseWithDomainObjects_When_EntitiesFound() {
            UUID warehouseId = UUID.randomUUID();
            UUID productId = UUID.randomUUID();
            StockMovement movement = new StockMovement(
                    UUID.randomUUID(), warehouseId, productId,
                    MovementType.ENTRY, 10, 100, 110,
                    null, null, null, null, null,
                    "Entry", LocalDateTime.now()
            );

            StockMovementEntity entity = StockMovementEntity.fromDomain(movement);
            Page<StockMovementEntity> page = new PageImpl<>(
                    List.of(entity), PageRequest.of(0, 10), 1);

            when(springDataRepository.findByWarehouseIdAndProductId(
                    eq(warehouseId), eq(productId), any(Pageable.class)))
                    .thenReturn(page);

            PageResponse<StockMovement> result = adapter.findByWarehouseIdAndProductId(
                    warehouseId, productId, 0, 10);

            assertNotNull(result);
            assertEquals(1, result.content().size());
            assertEquals(1, result.totalElements());
            assertEquals(1, result.totalPages());
            assertEquals(10, result.size());
            assertEquals(0, result.number());
            assertTrue(result.first());
            assertTrue(result.last());
            assertFalse(result.empty());

            StockMovement resultMovement = result.content().get(0);
            assertEquals(movement.getId(), resultMovement.getId());
            assertEquals(warehouseId, resultMovement.getWarehouseId());
            assertEquals(productId, resultMovement.getProductId());
            assertEquals(MovementType.ENTRY, resultMovement.getMovementType());

            verify(springDataRepository).findByWarehouseIdAndProductId(
                    eq(warehouseId), eq(productId), any(Pageable.class));
        }

        @Test
        void should_ReturnEmptyPageResponse_When_NoEntitiesFound() {
            UUID warehouseId = UUID.randomUUID();
            UUID productId = UUID.randomUUID();
            Page<StockMovementEntity> emptyPage = Page.empty();

            when(springDataRepository.findByWarehouseIdAndProductId(
                    eq(warehouseId), eq(productId), any(Pageable.class)))
                    .thenReturn(emptyPage);

            PageResponse<StockMovement> result = adapter.findByWarehouseIdAndProductId(
                    warehouseId, productId, 0, 10);

            assertNotNull(result);
            assertTrue(result.content().isEmpty());
            assertTrue(result.empty());
            assertEquals(0, result.totalElements());

            verify(springDataRepository).findByWarehouseIdAndProductId(
                    eq(warehouseId), eq(productId), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("FindByWarehouseId")
    class FindByWarehouseId {

        @Test
        void should_ReturnPageResponseWithDomainObjects_When_EntitiesFound() {
            UUID warehouseId = UUID.randomUUID();
            StockMovement movement = new StockMovement(
                    UUID.randomUUID(), warehouseId, UUID.randomUUID(),
                    MovementType.ENTRY, 10, 100, 110,
                    null, null, null, null, null,
                    "Entry", LocalDateTime.now()
            );

            StockMovementEntity entity = StockMovementEntity.fromDomain(movement);
            Page<StockMovementEntity> page = new PageImpl<>(
                    List.of(entity), PageRequest.of(0, 10), 1);

            when(springDataRepository.findByWarehouseId(eq(warehouseId), any(Pageable.class)))
                    .thenReturn(page);

            PageResponse<StockMovement> result = adapter.findByWarehouseId(warehouseId, 0, 10);

            assertNotNull(result);
            assertEquals(1, result.content().size());
            assertEquals(1, result.totalElements());
            assertFalse(result.empty());

            assertEquals(movement.getId(), result.content().get(0).getId());
            assertEquals(warehouseId, result.content().get(0).getWarehouseId());

            verify(springDataRepository).findByWarehouseId(eq(warehouseId), any(Pageable.class));
        }

        @Test
        void should_ReturnEmptyPageResponse_When_NoEntitiesFound() {
            UUID warehouseId = UUID.randomUUID();
            Page<StockMovementEntity> emptyPage = Page.empty();

            when(springDataRepository.findByWarehouseId(eq(warehouseId), any(Pageable.class)))
                    .thenReturn(emptyPage);

            PageResponse<StockMovement> result = adapter.findByWarehouseId(warehouseId, 0, 10);

            assertNotNull(result);
            assertTrue(result.content().isEmpty());
            assertTrue(result.empty());
            assertEquals(0, result.totalElements());

            verify(springDataRepository).findByWarehouseId(eq(warehouseId), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("FindByWarehouseIdAndMovementType")
    class FindByWarehouseIdAndMovementType {

        @Test
        void should_FilterByMovementTypeBeforeMappingThePage() {
            UUID warehouseId = UUID.randomUUID();
            StockMovement movement = new StockMovement(
                    UUID.randomUUID(), warehouseId, UUID.randomUUID(),
                    MovementType.EXIT, 4, 10, 6,
                    null, null, null, null, null,
                    "Exit", LocalDateTime.now()
            );
            Page<StockMovementEntity> page = new PageImpl<>(
                    List.of(StockMovementEntity.fromDomain(movement)), PageRequest.of(0, 10), 1);
            when(springDataRepository.findByWarehouseIdAndMovementType(
                    eq(warehouseId), eq(MovementType.EXIT), any(Pageable.class))).thenReturn(page);

            PageResponse<StockMovement> result = adapter.findByWarehouseIdAndMovementType(
                    warehouseId, MovementType.EXIT, 0, 10);

            assertEquals(1, result.totalElements());
            assertEquals(MovementType.EXIT, result.content().getFirst().getMovementType());
            verify(springDataRepository).findByWarehouseIdAndMovementType(
                    eq(warehouseId), eq(MovementType.EXIT), any(Pageable.class));
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
