package com.parsun.parsun_api.products.infrastructure.persistence.repositories;

import com.parsun.parsun_api.price_types.infrastructure.persistence.entities.PriceTypeEntity;
import com.parsun.parsun_api.price_types.infrastructure.persistence.repositories.SpringDataPriceTypeRepository;
import com.parsun.parsun_api.products.domain.entities.Product;
import com.parsun.parsun_api.products.domain.entities.ProductComponent;
import com.parsun.parsun_api.products.domain.valueobjects.ProductPrice;
import com.parsun.parsun_api.products.infrastructure.persistence.entities.ProductCodeSequenceEntity;
import com.parsun.parsun_api.products.infrastructure.persistence.entities.ProductEntity;
import com.parsun.parsun_api.shared.dto.PageResponse;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JpaProductRepository Unit Tests")
class JpaProductRepositoryTest {

    @Mock
    private SpringDataProductRepository springDataRepository;

    @Mock
    private SpringDataProductCodeSequenceRepository codeSequenceRepository;

    @Mock
    private SpringDataPriceTypeRepository springDataPriceTypeRepository;

    @Captor
    private ArgumentCaptor<ProductEntity> entityCaptor;

    private JpaProductRepository adapter;

    @BeforeEach
    void setUp() {
        adapter = new JpaProductRepository(springDataRepository, codeSequenceRepository, springDataPriceTypeRepository);
    }

    @Nested
    @DisplayName("Save")
    class Save {

        @Test
        void should_CreateNewProduct_When_IdIsNull() {
            Product product = Product.create("PRD-00001", "Test Product", "A test product",
                    UUID.randomUUID(), false, new BigDecimal("100.00"));

            ProductEntity savedEntity = ProductEntity.fromDomain(product);
            savedEntity.setId(UUID.randomUUID());
            when(springDataRepository.save(entityCaptor.capture())).thenReturn(savedEntity);

            Product result = adapter.save(product);

            assertNotNull(result);
            assertEquals(product.getName(), result.getName());
            assertEquals(product.getCode(), result.getCode());

            ProductEntity captured = entityCaptor.getValue();
            assertEquals(product.getName(), captured.getName());
            assertEquals(product.getCode(), captured.getCode());
            assertNotNull(captured.getId());

            verify(springDataRepository).save(any(ProductEntity.class));
            verifyNoInteractions(springDataPriceTypeRepository);
        }

        @Test
        void should_UpdateExistingProduct_When_IdIsNotNullAndEntityFound() {
            UUID id = UUID.randomUUID();
            Product product = Product.reconstitute(
                    id, "PRD-00001", "Updated Product", "Updated description",
                    UUID.randomUUID(), false, true, null,
                    new BigDecimal("120.00"),
                    List.of(), List.of(), LocalDateTime.now(), LocalDateTime.now());

            ProductEntity existingEntity = ProductEntity.fromDomain(product);

            when(springDataRepository.findWithComponentsById(id)).thenReturn(Optional.of(existingEntity));

            ProductEntity updatedEntity = ProductEntity.fromDomain(product);
            when(springDataRepository.save(entityCaptor.capture())).thenReturn(updatedEntity);

            Product result = adapter.save(product);

            assertNotNull(result);
            assertEquals("Updated Product", result.getName());
            assertEquals(new BigDecimal("120.00"), result.getPurchasePrice());

            verify(springDataRepository).findWithComponentsById(id);
            verify(springDataRepository).saveAndFlush(any(ProductEntity.class));
            verify(springDataRepository).save(any(ProductEntity.class));
            verifyNoInteractions(springDataPriceTypeRepository);
        }

        @Test
        void should_CreateNewProduct_When_IdIsNotNullButEntityNotFound() {
            UUID id = UUID.randomUUID();
            Product product = Product.reconstitute(
                    id, "PRD-00002", "Fallback Product", "Fallback description",
                    UUID.randomUUID(), false, true, null,
                    new BigDecimal("150.00"),
                    List.of(), List.of(), LocalDateTime.now(), LocalDateTime.now());

            when(springDataRepository.findWithComponentsById(id)).thenReturn(Optional.empty());

            ProductEntity savedEntity = ProductEntity.fromDomain(product);
            savedEntity.setId(UUID.randomUUID());
            when(springDataRepository.save(entityCaptor.capture())).thenReturn(savedEntity);

            Product result = adapter.save(product);

            assertNotNull(result);
            assertEquals("Fallback Product", result.getName());

            verify(springDataRepository).findWithComponentsById(id);
            verify(springDataRepository).save(any(ProductEntity.class));
            verifyNoInteractions(springDataPriceTypeRepository);
        }

        @Test
        void should_CreateNewProduct_When_IdIsNull_WithPrices() {
            UUID priceTypeId = UUID.randomUUID();
            ProductPrice price = ProductPrice.create(priceTypeId, new BigDecimal("50.00"), null);
            List<ProductPrice> prices = List.of(price);

            Product product = Product.reconstitute(
                    null, "PRD-00001", "Test Product", "A test product",
                    UUID.randomUUID(), false, true, null,
                    new BigDecimal("80.00"),
                    List.of(), prices, LocalDateTime.now(), LocalDateTime.now());

            PriceTypeEntity priceTypeEntity = new PriceTypeEntity();
            priceTypeEntity.setId(priceTypeId);
            when(springDataPriceTypeRepository.findAllById(List.of(priceTypeId)))
                    .thenReturn(List.of(priceTypeEntity));

            ProductEntity savedEntity = ProductEntity.fromDomain(product);
            savedEntity.setId(UUID.randomUUID());
            when(springDataRepository.save(entityCaptor.capture())).thenReturn(savedEntity);

            Product result = adapter.save(product);

            assertNotNull(result);
            assertEquals(product.getName(), result.getName());

            verify(springDataRepository).save(any(ProductEntity.class));
            verify(springDataPriceTypeRepository).findAllById(List.of(priceTypeId));
        }

        @Test
        void should_UpdateExistingProduct_When_IdIsNotNullAndEntityFound_WithPrices() {
            UUID id = UUID.randomUUID();
            UUID priceTypeId = UUID.randomUUID();
            ProductPrice price = ProductPrice.create(priceTypeId, new BigDecimal("50.00"), null);
            List<ProductPrice> prices = List.of(price);

            Product product = Product.reconstitute(
                    id, "PRD-00001", "Updated Product", "Updated description",
                    UUID.randomUUID(), false, true, null,
                    new BigDecimal("120.00"),
                    List.of(), prices, LocalDateTime.now(), LocalDateTime.now());

            ProductEntity existingEntity = ProductEntity.fromDomain(product);
            when(springDataRepository.findWithComponentsById(id)).thenReturn(Optional.of(existingEntity));

            PriceTypeEntity priceTypeEntity = new PriceTypeEntity();
            priceTypeEntity.setId(priceTypeId);
            when(springDataPriceTypeRepository.findAllById(List.of(priceTypeId)))
                    .thenReturn(List.of(priceTypeEntity));

            ProductEntity updatedEntity = ProductEntity.fromDomain(product);
            when(springDataRepository.save(entityCaptor.capture())).thenReturn(updatedEntity);

            Product result = adapter.save(product);

            assertNotNull(result);
            assertEquals("Updated Product", result.getName());
            assertEquals(new BigDecimal("120.00"), result.getPurchasePrice());

            verify(springDataRepository).findWithComponentsById(id);
            verify(springDataRepository).saveAndFlush(any(ProductEntity.class));
            verify(springDataRepository).save(any(ProductEntity.class));
            verify(springDataPriceTypeRepository).findAllById(List.of(priceTypeId));
        }

        @Test
        void should_CreateNewProduct_When_IdIsNotNullButEntityNotFound_WithPrices() {
            UUID id = UUID.randomUUID();
            UUID priceTypeId = UUID.randomUUID();
            ProductPrice price = ProductPrice.create(priceTypeId, new BigDecimal("50.00"), null);
            List<ProductPrice> prices = List.of(price);

            Product product = Product.reconstitute(
                    id, "PRD-00002", "Fallback Product", "Fallback description",
                    UUID.randomUUID(), false, true, null,
                    new BigDecimal("150.00"),
                    List.of(), prices, LocalDateTime.now(), LocalDateTime.now());

            when(springDataRepository.findWithComponentsById(id)).thenReturn(Optional.empty());

            PriceTypeEntity priceTypeEntity = new PriceTypeEntity();
            priceTypeEntity.setId(priceTypeId);
            when(springDataPriceTypeRepository.findAllById(List.of(priceTypeId)))
                    .thenReturn(List.of(priceTypeEntity));

            ProductEntity savedEntity = ProductEntity.fromDomain(product);
            savedEntity.setId(UUID.randomUUID());
            when(springDataRepository.save(entityCaptor.capture())).thenReturn(savedEntity);

            Product result = adapter.save(product);

            assertNotNull(result);
            assertEquals("Fallback Product", result.getName());

            verify(springDataRepository).findWithComponentsById(id);
            verify(springDataRepository).save(any(ProductEntity.class));
            verify(springDataPriceTypeRepository).findAllById(List.of(priceTypeId));
        }
    }

    @Nested
    @DisplayName("FindById")
    class FindById {

        @Test
        void should_ReturnDomainObject_When_EntityFound() {
            UUID id = UUID.randomUUID();
            Product product = Product.create("PRD-00001", "Test Product", "A test product",
                    UUID.randomUUID(), false, new BigDecimal("100.00"));
            ProductEntity entity = ProductEntity.fromDomain(product);
            entity.setId(id);

            when(springDataRepository.findWithComponentsById(id)).thenReturn(Optional.of(entity));

            Optional<Product> result = adapter.findById(id);

            assertTrue(result.isPresent());
            assertEquals(id, result.get().getId());
            verify(springDataRepository).findWithComponentsById(id);
        }

        @Test
        void should_ReturnEmpty_When_EntityNotFound() {
            UUID id = UUID.randomUUID();
            when(springDataRepository.findWithComponentsById(id)).thenReturn(Optional.empty());

            Optional<Product> result = adapter.findById(id);

            assertTrue(result.isEmpty());
            verify(springDataRepository).findWithComponentsById(id);
        }
    }

    @Nested
    @DisplayName("ExistsByCode")
    class ExistsByCode {

        @Test
        void should_ReturnTrue_When_CodeExists() {
            when(springDataRepository.existsByCode("PRD-00001")).thenReturn(true);

            boolean result = adapter.existsByCode("PRD-00001");

            assertTrue(result);
            verify(springDataRepository).existsByCode("PRD-00001");
        }

        @Test
        void should_ReturnFalse_When_CodeDoesNotExist() {
            when(springDataRepository.existsByCode("PRD-99999")).thenReturn(false);

            boolean result = adapter.existsByCode("PRD-99999");

            assertFalse(result);
            verify(springDataRepository).existsByCode("PRD-99999");
        }
    }

    @Nested
    @DisplayName("ExistsByCodeAndIdNot")
    class ExistsByCodeAndIdNot {

        @Test
        void should_ReturnTrue_When_CodeExistsForDifferentId() {
            UUID id = UUID.randomUUID();
            when(springDataRepository.existsByCodeAndIdNot("PRD-00001", id)).thenReturn(true);

            boolean result = adapter.existsByCodeAndIdNot("PRD-00001", id);

            assertTrue(result);
            verify(springDataRepository).existsByCodeAndIdNot("PRD-00001", id);
        }

        @Test
        void should_ReturnFalse_When_CodeOnlyMatchesSameId() {
            UUID id = UUID.randomUUID();
            when(springDataRepository.existsByCodeAndIdNot("PRD-00001", id)).thenReturn(false);

            boolean result = adapter.existsByCodeAndIdNot("PRD-00001", id);

            assertFalse(result);
            verify(springDataRepository).existsByCodeAndIdNot("PRD-00001", id);
        }
    }

    @Nested
    @DisplayName("ExistsByCategoryId")
    class ExistsByCategoryId {

        @Test
        void should_ReturnTrue_When_CategoryHasProducts() {
            UUID categoryId = UUID.randomUUID();
            when(springDataRepository.existsByCategoryId(categoryId)).thenReturn(true);

            boolean result = adapter.existsByCategoryId(categoryId);

            assertTrue(result);
            verify(springDataRepository).existsByCategoryId(categoryId);
        }

        @Test
        void should_ReturnFalse_When_CategoryHasNoProducts() {
            UUID categoryId = UUID.randomUUID();
            when(springDataRepository.existsByCategoryId(categoryId)).thenReturn(false);

            boolean result = adapter.existsByCategoryId(categoryId);

            assertFalse(result);
            verify(springDataRepository).existsByCategoryId(categoryId);
        }
    }

    @Nested
    @DisplayName("FindAllWithFilters")
    class FindAllWithFilters {

        @SuppressWarnings("unchecked")
        @Test
        void should_ReturnPagedResults_When_EntitiesFound() {
            Product product = Product.create("PRD-00001", "Test Product", "A test product",
                    UUID.randomUUID(), false, new BigDecimal("100.00"));

            Page<ProductEntity> entityPage = mock(Page.class);
            Page<Product> mappedPage = mock(Page.class);

            when(entityPage.map(any(Function.class))).thenReturn(mappedPage);
            when(mappedPage.getContent()).thenReturn(List.of(product));
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

            PageResponse<Product> result = adapter.findAllWithFilters(
                    null, null, null, null, 0, 20, "name", "asc");

            assertNotNull(result);
            assertEquals(1, result.totalElements());
            assertEquals(1, result.content().size());
            verify(springDataRepository).findAllWithFilters(
                    any(), any(), any(), any(), any(PageRequest.class));
        }

        @SuppressWarnings("unchecked")
        @Test
        void should_ReturnEmpty_When_NoEntitiesFound() {
            Page<ProductEntity> entityPage = mock(Page.class);
            Page<Product> mappedPage = mock(Page.class);

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

            PageResponse<Product> result = adapter.findAllWithFilters(
                    "NonExistent", null, true, false, 0, 20, "name", "asc");

            assertNotNull(result);
            assertTrue(result.empty());
            assertTrue(result.content().isEmpty());
            verify(springDataRepository).findAllWithFilters(
                    any(), any(), any(), any(), any(PageRequest.class));
        }

        @Test
        void should_DefaultToNameSort_When_InvalidOrderBy() {
            Page<ProductEntity> entityPage = mock(Page.class);
            Page<Product> mappedPage = mock(Page.class);

            when(entityPage.map(any(Function.class))).thenReturn(mappedPage);
            when(mappedPage.getContent()).thenReturn(List.of());
            when(mappedPage.getTotalElements()).thenReturn(0L);
            when(mappedPage.getTotalPages()).thenReturn(0);
            when(mappedPage.getSize()).thenReturn(20);
            when(mappedPage.getNumber()).thenReturn(0);
            when(mappedPage.isFirst()).thenReturn(true);
            when(mappedPage.isLast()).thenReturn(true);
            when(mappedPage.isEmpty()).thenReturn(true);

            ArgumentCaptor<PageRequest> pageCaptor = ArgumentCaptor.forClass(PageRequest.class);
            when(springDataRepository.findAllWithFilters(
                    any(), any(), any(), any(), pageCaptor.capture()))
                    .thenReturn(entityPage);

            PageResponse<Product> result = adapter.findAllWithFilters(
                    null, null, null, null, 0, 20, "invalid", "asc");

            PageRequest captured = pageCaptor.getValue();
            Sort.Order sortOrder = captured.getSort().iterator().next();
            assertEquals("name", sortOrder.getProperty());
            assertTrue(sortOrder.isAscending());
        }

        @Test
        void should_UseDescendingSort_When_DescOrderDir() {
            Page<ProductEntity> entityPage = mock(Page.class);
            Page<Product> mappedPage = mock(Page.class);

            when(entityPage.map(any(Function.class))).thenReturn(mappedPage);
            when(mappedPage.getContent()).thenReturn(List.of());
            when(mappedPage.getTotalElements()).thenReturn(0L);
            when(mappedPage.getTotalPages()).thenReturn(0);
            when(mappedPage.getSize()).thenReturn(20);
            when(mappedPage.getNumber()).thenReturn(0);
            when(mappedPage.isFirst()).thenReturn(true);
            when(mappedPage.isLast()).thenReturn(true);
            when(mappedPage.isEmpty()).thenReturn(true);

            ArgumentCaptor<PageRequest> pageCaptor = ArgumentCaptor.forClass(PageRequest.class);
            when(springDataRepository.findAllWithFilters(
                    any(), any(), any(), any(), pageCaptor.capture()))
                    .thenReturn(entityPage);

            PageResponse<Product> result = adapter.findAllWithFilters(
                    null, null, null, null, 0, 20, "name", "desc");

            PageRequest captured = pageCaptor.getValue();
            Sort.Order sortOrder = captured.getSort().iterator().next();
            assertEquals("name", sortOrder.getProperty());
            assertTrue(sortOrder.isDescending());
        }
    }

    @Nested
    @DisplayName("FindAllForOrder")
    class FindAllForOrder {

        @SuppressWarnings("unchecked")
        @Test
        void should_ReturnPagedResults_When_EntitiesFound() {
            Product product = Product.create("PRD-00001", "Test Product", "A test product",
                    UUID.randomUUID(), false, new BigDecimal("100.00"));

            Page<ProductEntity> entityPage = mock(Page.class);
            Page<Product> mappedPage = mock(Page.class);

            when(entityPage.map(any(Function.class))).thenReturn(mappedPage);
            when(mappedPage.getContent()).thenReturn(List.of(product));
            when(mappedPage.getTotalElements()).thenReturn(1L);
            when(mappedPage.getTotalPages()).thenReturn(1);
            when(mappedPage.getSize()).thenReturn(20);
            when(mappedPage.getNumber()).thenReturn(0);
            when(mappedPage.isFirst()).thenReturn(true);
            when(mappedPage.isLast()).thenReturn(true);
            when(mappedPage.isEmpty()).thenReturn(false);

            when(springDataRepository.findAllForOrder(
                    any(), any(), any(), any(), any(), any(PageRequest.class)))
                    .thenReturn(entityPage);

            Set<UUID> warehouseIds = Set.of(UUID.randomUUID());
            PageResponse<Product> result = adapter.findAllForOrder(
                    null, null, null, true, warehouseIds, 0, 20);

            assertNotNull(result);
            assertEquals(1, result.totalElements());
            assertEquals(1, result.content().size());
            verify(springDataRepository).findAllForOrder(
                    any(), any(), any(), any(), any(), any(PageRequest.class));
        }

        @SuppressWarnings("unchecked")
        @Test
        void should_ReturnEmpty_When_NoEntitiesFound() {
            Page<ProductEntity> entityPage = mock(Page.class);
            Page<Product> mappedPage = mock(Page.class);

            when(entityPage.map(any(Function.class))).thenReturn(mappedPage);
            when(mappedPage.getContent()).thenReturn(List.of());
            when(mappedPage.getTotalElements()).thenReturn(0L);
            when(mappedPage.getTotalPages()).thenReturn(0);
            when(mappedPage.getSize()).thenReturn(20);
            when(mappedPage.getNumber()).thenReturn(0);
            when(mappedPage.isFirst()).thenReturn(true);
            when(mappedPage.isLast()).thenReturn(true);
            when(mappedPage.isEmpty()).thenReturn(true);

            when(springDataRepository.findAllForOrder(
                    any(), any(), any(), any(), any(), any(PageRequest.class)))
                    .thenReturn(entityPage);

            PageResponse<Product> result = adapter.findAllForOrder(
                    null, null, null, false, Set.of(), 0, 20);

            assertNotNull(result);
            assertTrue(result.empty());
            assertTrue(result.content().isEmpty());
            verify(springDataRepository).findAllForOrder(
                    any(), any(), any(), any(), any(), any(PageRequest.class));
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

    @Nested
    @DisplayName("DeleteComponentReferencesByProductId")
    class DeleteComponentReferencesByProductId {

        @Test
        void should_DelegateToSpringDataRepository() {
            UUID productId = UUID.randomUUID();

            adapter.deleteComponentReferencesByProductId(productId);

            verify(springDataRepository).deleteByComponentProductId(productId);
        }
    }

    @Nested
    @DisplayName("GenerateNextCode")
    class GenerateNextCode {

        @Test
        void should_SaveSequenceAndReturnFormattedCode() {
            ProductCodeSequenceEntity savedSeq = mock(ProductCodeSequenceEntity.class);
            when(savedSeq.getId()).thenReturn(1L);
            when(codeSequenceRepository.save(any(ProductCodeSequenceEntity.class))).thenReturn(savedSeq);

            String code = adapter.generateNextCode();

            assertEquals("PRD-00001", code);
            verify(codeSequenceRepository).save(any(ProductCodeSequenceEntity.class));
        }

        @Test
        void should_FormatCodeWithLeadingZeros() {
            ProductCodeSequenceEntity savedSeq = mock(ProductCodeSequenceEntity.class);
            when(savedSeq.getId()).thenReturn(42L);
            when(codeSequenceRepository.save(any(ProductCodeSequenceEntity.class))).thenReturn(savedSeq);

            String code = adapter.generateNextCode();

            assertEquals("PRD-00042", code);
            verify(codeSequenceRepository).save(any(ProductCodeSequenceEntity.class));
        }
    }
}
