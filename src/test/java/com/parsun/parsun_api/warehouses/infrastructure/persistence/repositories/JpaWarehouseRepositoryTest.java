package com.parsun.parsun_api.warehouses.infrastructure.persistence.repositories;

import com.parsun.parsun_api.shared.dto.PageResponse;
import com.parsun.parsun_api.warehouses.domain.entities.Warehouse;
import com.parsun.parsun_api.warehouses.infrastructure.persistence.entities.WarehouseEntity;
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
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JpaWarehouseRepository Unit Tests")
class JpaWarehouseRepositoryTest {

    @Mock
    private SpringDataWarehouseRepository springDataRepository;

    @Captor
    private ArgumentCaptor<WarehouseEntity> entityCaptor;

    private JpaWarehouseRepository adapter;

    @BeforeEach
    void setUp() {
        adapter = new JpaWarehouseRepository(springDataRepository);
    }

    @Nested
    @DisplayName("Save")
    class Save {

        @Test
        void should_CreateNewWarehouse_When_IdIsNull() {
            Warehouse warehouse = Warehouse.create("WH-01", "Main Warehouse", "123 Main St",
                    "California", "Los Angeles", "90001");

            WarehouseEntity savedEntity = WarehouseEntity.fromDomain(warehouse);
            when(springDataRepository.save(entityCaptor.capture())).thenReturn(savedEntity);

            Warehouse result = adapter.save(warehouse);

            assertNotNull(result);
            assertEquals(warehouse.getCode(), result.getCode());
            assertEquals(warehouse.getName(), result.getName());

            WarehouseEntity captured = entityCaptor.getValue();
            assertEquals(warehouse.getCode(), captured.getCode());
            assertEquals(warehouse.getName(), captured.getName());
            assertEquals(warehouse.getAddress(), captured.getAddress());

            verify(springDataRepository).save(any(WarehouseEntity.class));
        }

        @Test
        void should_UpdateExistingWarehouse_When_IdIsNotNullAndEntityExists() {
            UUID id = UUID.randomUUID();
            Warehouse warehouse = Warehouse.reconstitute(id, "WH-01", "Updated Warehouse",
                    "456 Oak Ave", "California", "San Francisco", "94102",
                    true, LocalDateTime.now(), LocalDateTime.now());

            WarehouseEntity existingEntity = WarehouseEntity.fromDomain(warehouse);

            when(springDataRepository.findById(id)).thenReturn(Optional.of(existingEntity));
            when(springDataRepository.save(entityCaptor.capture())).thenReturn(existingEntity);

            Warehouse result = adapter.save(warehouse);

            assertNotNull(result);
            assertEquals("Updated Warehouse", result.getName());
            assertEquals("456 Oak Ave", result.getAddress());

            WarehouseEntity captured = entityCaptor.getValue();
            assertEquals("Updated Warehouse", captured.getName());
            assertEquals("456 Oak Ave", captured.getAddress());

            verify(springDataRepository).findById(id);
            verify(springDataRepository).save(any(WarehouseEntity.class));
        }

        @Test
        void should_CreateNewWarehouse_When_IdIsNotNullButEntityNotFound() {
            UUID id = UUID.randomUUID();
            Warehouse warehouse = Warehouse.reconstitute(id, "WH-02", "New Warehouse",
                    "789 Pine St", "Texas", "Dallas", "75001",
                    true, LocalDateTime.now(), LocalDateTime.now());

            when(springDataRepository.findById(id)).thenReturn(Optional.empty());

            WarehouseEntity savedEntity = WarehouseEntity.fromDomain(warehouse);
            when(springDataRepository.save(entityCaptor.capture())).thenReturn(savedEntity);

            Warehouse result = adapter.save(warehouse);

            assertNotNull(result);
            assertEquals("WH-02", result.getCode());
            assertEquals("New Warehouse", result.getName());

            verify(springDataRepository).findById(id);
            verify(springDataRepository).save(any(WarehouseEntity.class));
        }
    }

    @Nested
    @DisplayName("FindById")
    class FindById {

        @Test
        void should_ReturnDomainObject_When_EntityFound() {
            UUID id = UUID.randomUUID();
            Warehouse warehouse = Warehouse.reconstitute(id, "WH-01", "Main Warehouse",
                    "123 Main St", "California", "Los Angeles", "90001",
                    true, LocalDateTime.now(), LocalDateTime.now());
            WarehouseEntity entity = WarehouseEntity.fromDomain(warehouse);

            when(springDataRepository.findById(id)).thenReturn(Optional.of(entity));

            Optional<Warehouse> result = adapter.findById(id);

            assertTrue(result.isPresent());
            assertEquals(id, result.get().getId());
            assertEquals("WH-01", result.get().getCode());
            verify(springDataRepository).findById(id);
        }

        @Test
        void should_ReturnEmpty_When_EntityNotFound() {
            UUID id = UUID.randomUUID();
            when(springDataRepository.findById(id)).thenReturn(Optional.empty());

            Optional<Warehouse> result = adapter.findById(id);

            assertTrue(result.isEmpty());
            verify(springDataRepository).findById(id);
        }
    }

    @Nested
    @DisplayName("ExistsByCode")
    class ExistsByCode {

        @Test
        void should_ReturnTrue_When_CodeExists() {
            when(springDataRepository.existsByCode("WH-01")).thenReturn(true);

            boolean result = adapter.existsByCode("WH-01");

            assertTrue(result);
            verify(springDataRepository).existsByCode("WH-01");
        }

        @Test
        void should_ReturnFalse_When_CodeDoesNotExist() {
            when(springDataRepository.existsByCode("WH-99")).thenReturn(false);

            boolean result = adapter.existsByCode("WH-99");

            assertFalse(result);
            verify(springDataRepository).existsByCode("WH-99");
        }
    }

    @Nested
    @DisplayName("ExistsByCodeAndIdNot")
    class ExistsByCodeAndIdNot {

        @Test
        void should_ReturnTrue_When_CodeExistsForDifferentId() {
            UUID id = UUID.randomUUID();
            when(springDataRepository.existsByCodeAndIdNot("WH-01", id)).thenReturn(true);

            boolean result = adapter.existsByCodeAndIdNot("WH-01", id);

            assertTrue(result);
            verify(springDataRepository).existsByCodeAndIdNot("WH-01", id);
        }

        @Test
        void should_ReturnFalse_When_CodeOnlyMatchesSameId() {
            UUID id = UUID.randomUUID();
            when(springDataRepository.existsByCodeAndIdNot("WH-01", id)).thenReturn(false);

            boolean result = adapter.existsByCodeAndIdNot("WH-01", id);

            assertFalse(result);
            verify(springDataRepository).existsByCodeAndIdNot("WH-01", id);
        }
    }

    @Nested
    @DisplayName("FindAllWithFilters")
    class FindAllWithFilters {

        @SuppressWarnings("unchecked")
        @Test
        void should_ReturnPagedResults_When_EntitiesFound() {
            Warehouse warehouse = Warehouse.reconstitute(UUID.randomUUID(), "WH-01", "Main Warehouse",
                    "123 Main St", "California", "Los Angeles", "90001",
                    true, LocalDateTime.now(), LocalDateTime.now());

            Page<WarehouseEntity> entityPage = mock(Page.class);
            Page<Warehouse> mappedPage = mock(Page.class);

            when(entityPage.map(any(Function.class))).thenReturn(mappedPage);
            when(mappedPage.getContent()).thenReturn(List.of(warehouse));
            when(mappedPage.getTotalElements()).thenReturn(1L);
            when(mappedPage.getTotalPages()).thenReturn(1);
            when(mappedPage.getSize()).thenReturn(20);
            when(mappedPage.getNumber()).thenReturn(0);
            when(mappedPage.isFirst()).thenReturn(true);
            when(mappedPage.isLast()).thenReturn(true);
            when(mappedPage.isEmpty()).thenReturn(false);

            when(springDataRepository.findAllWithFilters(any(), any(), any(PageRequest.class)))
                    .thenReturn(entityPage);

            PageResponse<Warehouse> result = adapter.findAllWithFilters(
                    null, null, 0, 20, "name", "asc");

            assertNotNull(result);
            assertEquals(1, result.totalElements());
            assertEquals(1, result.content().size());
            assertEquals(warehouse.getId(), result.content().get(0).getId());
            verify(springDataRepository).findAllWithFilters(any(), any(), any(PageRequest.class));
        }

        @SuppressWarnings("unchecked")
        @Test
        void should_ReturnEmpty_When_NoEntitiesFound() {
            Page<WarehouseEntity> entityPage = mock(Page.class);
            Page<Warehouse> mappedPage = mock(Page.class);

            when(entityPage.map(any(Function.class))).thenReturn(mappedPage);
            when(mappedPage.getContent()).thenReturn(List.of());
            when(mappedPage.getTotalElements()).thenReturn(0L);
            when(mappedPage.getTotalPages()).thenReturn(0);
            when(mappedPage.getSize()).thenReturn(20);
            when(mappedPage.getNumber()).thenReturn(0);
            when(mappedPage.isFirst()).thenReturn(true);
            when(mappedPage.isLast()).thenReturn(true);
            when(mappedPage.isEmpty()).thenReturn(true);

            when(springDataRepository.findAllWithFilters(any(), any(), any(PageRequest.class)))
                    .thenReturn(entityPage);

            PageResponse<Warehouse> result = adapter.findAllWithFilters(
                    "NonExistent", true, 0, 20, "name", "asc");

            assertNotNull(result);
            assertTrue(result.empty());
            assertTrue(result.content().isEmpty());
            verify(springDataRepository).findAllWithFilters(any(), any(), any(PageRequest.class));
        }

        @SuppressWarnings("unchecked")
        @Test
        void should_DefaultToNameSort_When_InvalidOrderBy() {
            Page<WarehouseEntity> entityPage = mock(Page.class);
            Page<Warehouse> mappedPage = mock(Page.class);

            when(entityPage.map(any(Function.class))).thenReturn(mappedPage);
            when(mappedPage.getContent()).thenReturn(List.of());
            when(mappedPage.getTotalElements()).thenReturn(0L);
            when(mappedPage.getTotalPages()).thenReturn(0);
            when(mappedPage.getSize()).thenReturn(20);
            when(mappedPage.getNumber()).thenReturn(0);
            when(mappedPage.isFirst()).thenReturn(true);
            when(mappedPage.isLast()).thenReturn(true);
            when(mappedPage.isEmpty()).thenReturn(true);

            when(springDataRepository.findAllWithFilters(any(), any(), any(PageRequest.class)))
                    .thenReturn(entityPage);

            adapter.findAllWithFilters(null, null, 0, 20, "invalidField", "asc");

            ArgumentCaptor<PageRequest> captor = ArgumentCaptor.forClass(PageRequest.class);
            verify(springDataRepository).findAllWithFilters(any(), any(), captor.capture());

            PageRequest captured = captor.getValue();
            Sort.Order order = captured.getSort().getOrderFor("name");
            assertNotNull(order, "Should fallback to 'name' sort when orderBy is invalid");
            assertEquals(Sort.Direction.ASC, order.getDirection());
        }

        @SuppressWarnings("unchecked")
        @Test
        void should_UseDescendingSort_When_DescOrderDir() {
            Page<WarehouseEntity> entityPage = mock(Page.class);
            Page<Warehouse> mappedPage = mock(Page.class);

            when(entityPage.map(any(Function.class))).thenReturn(mappedPage);
            when(mappedPage.getContent()).thenReturn(List.of());
            when(mappedPage.getTotalElements()).thenReturn(0L);
            when(mappedPage.getTotalPages()).thenReturn(0);
            when(mappedPage.getSize()).thenReturn(20);
            when(mappedPage.getNumber()).thenReturn(0);
            when(mappedPage.isFirst()).thenReturn(true);
            when(mappedPage.isLast()).thenReturn(true);
            when(mappedPage.isEmpty()).thenReturn(true);

            when(springDataRepository.findAllWithFilters(any(), any(), any(PageRequest.class)))
                    .thenReturn(entityPage);

            adapter.findAllWithFilters(null, null, 0, 20, "name", "desc");

            ArgumentCaptor<PageRequest> captor = ArgumentCaptor.forClass(PageRequest.class);
            verify(springDataRepository).findAllWithFilters(any(), any(), captor.capture());

            PageRequest captured = captor.getValue();
            Sort.Order order = captured.getSort().getOrderFor("name");
            assertNotNull(order);
            assertEquals(Sort.Direction.DESC, order.getDirection());
        }

        @SuppressWarnings("unchecked")
        @Test
        void should_SortByCode() {
            Page<WarehouseEntity> entityPage = mock(Page.class);
            Page<Warehouse> mappedPage = mock(Page.class);

            when(entityPage.map(any(Function.class))).thenReturn(mappedPage);
            when(mappedPage.getContent()).thenReturn(List.of());
            when(mappedPage.getTotalElements()).thenReturn(0L);
            when(mappedPage.getTotalPages()).thenReturn(0);
            when(mappedPage.getSize()).thenReturn(20);
            when(mappedPage.getNumber()).thenReturn(0);
            when(mappedPage.isFirst()).thenReturn(true);
            when(mappedPage.isLast()).thenReturn(true);
            when(mappedPage.isEmpty()).thenReturn(true);

            when(springDataRepository.findAllWithFilters(any(), any(), any(PageRequest.class)))
                    .thenReturn(entityPage);

            adapter.findAllWithFilters(null, null, 0, 20, "code", "asc");

            ArgumentCaptor<PageRequest> captor = ArgumentCaptor.forClass(PageRequest.class);
            verify(springDataRepository).findAllWithFilters(any(), any(), captor.capture());

            PageRequest captured = captor.getValue();
            Sort.Order order = captured.getSort().getOrderFor("code");
            assertNotNull(order);
            assertEquals(Sort.Direction.ASC, order.getDirection());
        }

        @SuppressWarnings("unchecked")
        @Test
        void should_SortByState() {
            Page<WarehouseEntity> entityPage = mock(Page.class);
            Page<Warehouse> mappedPage = mock(Page.class);

            when(entityPage.map(any(Function.class))).thenReturn(mappedPage);
            when(mappedPage.getContent()).thenReturn(List.of());
            when(mappedPage.getTotalElements()).thenReturn(0L);
            when(mappedPage.getTotalPages()).thenReturn(0);
            when(mappedPage.getSize()).thenReturn(20);
            when(mappedPage.getNumber()).thenReturn(0);
            when(mappedPage.isFirst()).thenReturn(true);
            when(mappedPage.isLast()).thenReturn(true);
            when(mappedPage.isEmpty()).thenReturn(true);

            when(springDataRepository.findAllWithFilters(any(), any(), any(PageRequest.class)))
                    .thenReturn(entityPage);

            adapter.findAllWithFilters(null, null, 0, 20, "state", "asc");

            ArgumentCaptor<PageRequest> captor = ArgumentCaptor.forClass(PageRequest.class);
            verify(springDataRepository).findAllWithFilters(any(), any(), captor.capture());

            PageRequest captured = captor.getValue();
            Sort.Order order = captured.getSort().getOrderFor("state");
            assertNotNull(order);
            assertEquals(Sort.Direction.ASC, order.getDirection());
        }

        @SuppressWarnings("unchecked")
        @Test
        void should_SortByCreatedAt() {
            Page<WarehouseEntity> entityPage = mock(Page.class);
            Page<Warehouse> mappedPage = mock(Page.class);

            when(entityPage.map(any(Function.class))).thenReturn(mappedPage);
            when(mappedPage.getContent()).thenReturn(List.of());
            when(mappedPage.getTotalElements()).thenReturn(0L);
            when(mappedPage.getTotalPages()).thenReturn(0);
            when(mappedPage.getSize()).thenReturn(20);
            when(mappedPage.getNumber()).thenReturn(0);
            when(mappedPage.isFirst()).thenReturn(true);
            when(mappedPage.isLast()).thenReturn(true);
            when(mappedPage.isEmpty()).thenReturn(true);

            when(springDataRepository.findAllWithFilters(any(), any(), any(PageRequest.class)))
                    .thenReturn(entityPage);

            adapter.findAllWithFilters(null, null, 0, 20, "createdAt", "asc");

            ArgumentCaptor<PageRequest> captor = ArgumentCaptor.forClass(PageRequest.class);
            verify(springDataRepository).findAllWithFilters(any(), any(), captor.capture());

            PageRequest captured = captor.getValue();
            Sort.Order order = captured.getSort().getOrderFor("createdAt");
            assertNotNull(order);
            assertEquals(Sort.Direction.ASC, order.getDirection());
        }
    }

    @Nested
    @DisplayName("Delete")
    class Delete {

        @Test
        void should_DelegateToSpringDataRepository() {
            UUID id = UUID.randomUUID();

            adapter.delete(id);

            verify(springDataRepository).deleteById(id);
        }
    }
}
