package com.cashly.cashly_api.suppliers.infrastructure.persistence.repositories;

import com.cashly.cashly_api.shared.dto.PageResponse;
import com.cashly.cashly_api.suppliers.domain.entities.Supplier;
import com.cashly.cashly_api.suppliers.domain.valueobjects.*;
import com.cashly.cashly_api.suppliers.infrastructure.persistence.entities.SupplierEntity;
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
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JpaSupplierRepository Unit Tests")
class JpaSupplierRepositoryTest {

    @Mock
    private SpringDataSupplierRepository springDataRepository;

    @Captor
    private ArgumentCaptor<SupplierEntity> entityCaptor;

    private JpaSupplierRepository adapter;

    @BeforeEach
    void setUp() {
        adapter = new JpaSupplierRepository(springDataRepository);
    }

    @Nested
    @DisplayName("Save")
    class Save {

        @Test
        void should_CreateNewSupplier_When_NotExisting() {
            Supplier supplier = createTestSupplier();

            when(springDataRepository.findById(supplier.getId().getValue())).thenReturn(Optional.empty());

            SupplierEntity savedEntity = SupplierEntity.fromDomain(supplier);
            when(springDataRepository.save(entityCaptor.capture())).thenReturn(savedEntity);

            Supplier result = adapter.save(supplier);

            assertNotNull(result);
            assertEquals(supplier.getId(), result.getId());
            assertEquals(supplier.getLegalName(), result.getLegalName());
            assertEquals(supplier.getRif(), result.getRif());
            assertEquals(supplier.getAlias(), result.getAlias());

            SupplierEntity captured = entityCaptor.getValue();
            assertEquals(supplier.getId().getValue(), captured.getId());
            assertEquals(supplier.getCode().getValue(), captured.getCode());
            assertEquals(supplier.getLegalName().getValue(), captured.getLegalName());

            verify(springDataRepository).findById(supplier.getId().getValue());
            verify(springDataRepository).save(any(SupplierEntity.class));
        }

        @Test
        void should_UpdateExistingSupplier_When_AlreadyExists() {
            Supplier supplier = createTestSupplier();
            SupplierEntity existingEntity = SupplierEntity.fromDomain(supplier);

            when(springDataRepository.findById(supplier.getId().getValue())).thenReturn(Optional.of(existingEntity));
            when(springDataRepository.save(entityCaptor.capture())).thenReturn(existingEntity);

            Supplier result = adapter.save(supplier);

            assertNotNull(result);
            assertEquals(supplier.getId(), result.getId());

            SupplierEntity captured = entityCaptor.getValue();
            assertEquals(supplier.getLegalName().getValue(), captured.getLegalName());
            assertEquals(supplier.getRif().getValue(), captured.getRif());

            verify(springDataRepository).findById(supplier.getId().getValue());
            verify(springDataRepository).save(any(SupplierEntity.class));
        }
    }

    @Nested
    @DisplayName("FindById")
    class FindById {

        @Test
        void should_ReturnDomainObject_When_EntityFound() {
            Supplier supplier = createTestSupplier();
            SupplierEntity entity = SupplierEntity.fromDomain(supplier);

            when(springDataRepository.findById(supplier.getId().getValue())).thenReturn(Optional.of(entity));

            Optional<Supplier> result = adapter.findById(supplier.getId());

            assertTrue(result.isPresent());
            assertEquals(supplier.getId(), result.get().getId());
            assertEquals(supplier.getLegalName(), result.get().getLegalName());
            verify(springDataRepository).findById(supplier.getId().getValue());
        }

        @Test
        void should_ReturnEmpty_When_EntityNotFound() {
            SupplierId id = SupplierId.generate();
            when(springDataRepository.findById(id.getValue())).thenReturn(Optional.empty());

            Optional<Supplier> result = adapter.findById(id);

            assertTrue(result.isEmpty());
            verify(springDataRepository).findById(id.getValue());
        }
    }

    @Nested
    @DisplayName("FindAll")
    class FindAll {

        @Test
        void should_ReturnAllSuppliers() {
            Supplier supplier = createTestSupplier();
            SupplierEntity entity = SupplierEntity.fromDomain(supplier);

            when(springDataRepository.findAll()).thenReturn(List.of(entity));

            List<Supplier> result = adapter.findAll();

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(supplier.getId(), result.get(0).getId());
            verify(springDataRepository).findAll();
        }
    }

    @Nested
    @DisplayName("FindAllWithFilters")
    class FindAllWithFilters {

        @SuppressWarnings("unchecked")
        @Test
        void should_ReturnPagedResults_When_EntitiesFound() {
            Supplier supplier = createTestSupplier();
            Page<SupplierEntity> entityPage = mock(Page.class);
            Page<Supplier> mappedPage = mock(Page.class);

            when(entityPage.map(any(Function.class))).thenReturn(mappedPage);
            when(mappedPage.getContent()).thenReturn(List.of(supplier));
            when(mappedPage.getTotalElements()).thenReturn(1L);
            when(mappedPage.getTotalPages()).thenReturn(1);
            when(mappedPage.getSize()).thenReturn(20);
            when(mappedPage.getNumber()).thenReturn(0);
            when(mappedPage.isFirst()).thenReturn(true);
            when(mappedPage.isLast()).thenReturn(true);
            when(mappedPage.isEmpty()).thenReturn(false);

            when(springDataRepository.findAllWithFilters(
                    any(), any(), any(), any(), any(PageRequest.class)))
                    .thenReturn(entityPage);

            PageResponse<Supplier> result = adapter.findAllWithFilters(
                    null, null, null, null, 0, 20, "legalName", "asc");

            assertNotNull(result);
            assertEquals(1, result.totalElements());
            assertEquals(1, result.content().size());
            assertEquals(supplier.getId(), result.content().get(0).getId());
            verify(springDataRepository).findAllWithFilters(
                    any(), any(), any(), any(), any(PageRequest.class));
        }

        @SuppressWarnings("unchecked")
        @Test
        void should_ReturnEmpty_When_NoEntitiesFound() {
            Page<SupplierEntity> entityPage = mock(Page.class);
            Page<Supplier> mappedPage = mock(Page.class);

            when(entityPage.map(any(Function.class))).thenReturn(mappedPage);
            when(mappedPage.getContent()).thenReturn(List.of());
            when(mappedPage.getTotalElements()).thenReturn(0L);
            when(mappedPage.getTotalPages()).thenReturn(0);
            when(mappedPage.getSize()).thenReturn(20);
            when(mappedPage.getNumber()).thenReturn(0);
            when(mappedPage.isFirst()).thenReturn(true);
            when(mappedPage.isLast()).thenReturn(true);
            when(mappedPage.isEmpty()).thenReturn(true);

            when(springDataRepository.findAllWithFilters(
                    any(), any(), any(), any(), any(PageRequest.class)))
                    .thenReturn(entityPage);

            PageResponse<Supplier> result = adapter.findAllWithFilters(
                    null, null, null, null, 0, 20, "legalName", "asc");

            assertNotNull(result);
            assertTrue(result.empty());
            assertTrue(result.content().isEmpty());
            verify(springDataRepository).findAllWithFilters(
                    any(), any(), any(), any(), any(PageRequest.class));
        }

        @SuppressWarnings("unchecked")
        @Test
        void should_FallbackToLegalName_When_InvalidOrderByProvided() {
            Supplier supplier = createTestSupplier();
            Page<SupplierEntity> entityPage = mock(Page.class);
            Page<Supplier> mappedPage = mock(Page.class);

            when(entityPage.map(any(Function.class))).thenReturn(mappedPage);
            when(mappedPage.getContent()).thenReturn(List.of(supplier));
            when(mappedPage.getTotalElements()).thenReturn(1L);
            when(mappedPage.getTotalPages()).thenReturn(1);
            when(mappedPage.getSize()).thenReturn(20);
            when(mappedPage.getNumber()).thenReturn(0);
            when(mappedPage.isFirst()).thenReturn(true);
            when(mappedPage.isLast()).thenReturn(true);
            when(mappedPage.isEmpty()).thenReturn(false);

            when(springDataRepository.findAllWithFilters(
                    any(), any(), any(), any(), any(PageRequest.class)))
                    .thenReturn(entityPage);

            PageResponse<Supplier> result = adapter.findAllWithFilters(
                    null, null, null, null, 0, 20, "invalidField", "asc");

            assertNotNull(result);
            assertEquals(1, result.totalElements());
            verify(springDataRepository).findAllWithFilters(
                    any(), any(), any(), any(), any(PageRequest.class));
        }

        @SuppressWarnings("unchecked")
        @Test
        void should_UseDescendingSort_When_DescDirectionProvided() {
            Supplier supplier = createTestSupplier();
            Page<SupplierEntity> entityPage = mock(Page.class);
            Page<Supplier> mappedPage = mock(Page.class);

            when(entityPage.map(any(Function.class))).thenReturn(mappedPage);
            when(mappedPage.getContent()).thenReturn(List.of(supplier));
            when(mappedPage.getTotalElements()).thenReturn(1L);
            when(mappedPage.getTotalPages()).thenReturn(1);
            when(mappedPage.getSize()).thenReturn(20);
            when(mappedPage.getNumber()).thenReturn(0);
            when(mappedPage.isFirst()).thenReturn(true);
            when(mappedPage.isLast()).thenReturn(true);
            when(mappedPage.isEmpty()).thenReturn(false);

            when(springDataRepository.findAllWithFilters(
                    any(), any(), any(), any(), any(PageRequest.class)))
                    .thenReturn(entityPage);

            PageResponse<Supplier> result = adapter.findAllWithFilters(
                    null, null, null, null, 0, 20, "legalName", "desc");

            assertNotNull(result);
            assertEquals(1, result.totalElements());
            assertEquals(supplier.getId(), result.content().get(0).getId());
            verify(springDataRepository).findAllWithFilters(
                    any(), any(), any(), any(), any(PageRequest.class));
        }
    }

    @Nested
    @DisplayName("ExistsById")
    class ExistsById {

        @Test
        void should_ReturnTrue_When_SupplierExists() {
            SupplierId id = SupplierId.generate();
            when(springDataRepository.existsById(id.getValue())).thenReturn(true);

            boolean result = adapter.existsById(id);

            assertTrue(result);
            verify(springDataRepository).existsById(id.getValue());
        }

        @Test
        void should_ReturnFalse_When_SupplierDoesNotExist() {
            SupplierId id = SupplierId.generate();
            when(springDataRepository.existsById(id.getValue())).thenReturn(false);

            boolean result = adapter.existsById(id);

            assertFalse(result);
            verify(springDataRepository).existsById(id.getValue());
        }
    }

    @Nested
    @DisplayName("Delete")
    class Delete {

        @Test
        void should_DelegateToSpringDataRepository() {
            SupplierId id = SupplierId.generate();

            adapter.delete(id);

            verify(springDataRepository).deleteById(id.getValue());
        }
    }

    @Nested
    @DisplayName("GetNextCode")
    class GetNextCode {

        @Test
        void should_ReturnMaxCodePlusOne_When_MaxCodeExists() {
            when(springDataRepository.findMaxCode()).thenReturn(Optional.of(42L));

            long next = adapter.getNextCode();

            assertEquals(43L, next);
            verify(springDataRepository).findMaxCode();
        }

        @Test
        void should_ReturnOne_When_NoMaxCode() {
            when(springDataRepository.findMaxCode()).thenReturn(Optional.empty());

            long next = adapter.getNextCode();

            assertEquals(1L, next);
            verify(springDataRepository).findMaxCode();
        }
    }

    private Supplier createTestSupplier() {
        return Supplier.reconstitute(
            SupplierId.generate(),
            SupplierCode.from(1L),
            new LegalName("Test Supplier S.A."),
            SupplierType.JURIDICO,
            new Rif("J-12345678-9"),
            new SupplierAlias("TestSupplier"),
            null,
            true,
            LocalDateTime.now(),
            LocalDateTime.now(),
            List.of(),
            List.of(),
            List.of()
        );
    }
}
