package com.cashly.cashly_api.categories.infrastructure.persistence.repositories;

import com.cashly.cashly_api.categories.domain.entities.Category;
import com.cashly.cashly_api.categories.domain.repositories.CategoryRepository.CategorySummary;
import com.cashly.cashly_api.categories.infrastructure.persistence.entities.CategoryEntity;
import com.cashly.cashly_api.shared.dto.PageResponse;
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
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JpaCategoryRepository Unit Tests")
class JpaCategoryRepositoryTest {

    @Mock
    private SpringDataCategoryRepository springDataRepository;

    @Captor
    private ArgumentCaptor<CategoryEntity> entityCaptor;

    @Captor
    private ArgumentCaptor<PageRequest> pageRequestCaptor;

    private JpaCategoryRepository repository;

    @BeforeEach
    void setUp() {
        repository = new JpaCategoryRepository(springDataRepository);
    }

    @Nested
    @DisplayName("Save")
    class Save {

        @Test
        void should_CreateNewEntity_When_IdIsNull() {
            Category category = Category.create("Electronics", "Electronic items");
            UUID generatedId = UUID.randomUUID();

            CategoryEntity entityWithoutId = CategoryEntity.fromDomain(category);
            entityWithoutId.setId(generatedId);

            when(springDataRepository.save(any(CategoryEntity.class))).thenReturn(entityWithoutId);

            Category saved = repository.save(category);

            assertNotNull(saved);
            assertNotNull(saved.getId());
            assertEquals(category.getName(), saved.getName());
            assertEquals(category.getDescription(), saved.getDescription());
            assertTrue(saved.isActive());

            verify(springDataRepository).save(entityCaptor.capture());
            CategoryEntity captured = entityCaptor.getValue();
            assertNotNull(captured.getId());
            assertEquals(category.getName(), captured.getName());
            assertEquals(category.getDescription(), captured.getDescription());
            assertTrue(captured.isActive());
        }

        @Test
        void should_UpdateExistingEntity_When_IdIsNotNullAndFound() {
            UUID id = UUID.randomUUID();
            Category existing = Category.reconstitute(
                    id, "Old Name", "Old Description", true,
                    LocalDateTime.now().minusDays(1), LocalDateTime.now().minusDays(1)
            );
            existing.update("Updated Name", "Updated Description", true);

            CategoryEntity existingEntity = CategoryEntity.fromDomain(existing);
            when(springDataRepository.findById(id)).thenReturn(Optional.of(existingEntity));
            when(springDataRepository.save(any(CategoryEntity.class))).thenReturn(existingEntity);

            Category saved = repository.save(existing);

            assertNotNull(saved);
            assertEquals("Updated Name", saved.getName());
            assertEquals("Updated Description", saved.getDescription());
            verify(springDataRepository).findById(id);
            verify(springDataRepository).save(existingEntity);
        }

        @Test
        void should_CreateNewEntity_When_IdIsNotNullButNotFound() {
            UUID id = UUID.randomUUID();
            Category category = Category.reconstitute(
                    id, "New Name", "New Description", true,
                    LocalDateTime.now(), LocalDateTime.now()
            );

            when(springDataRepository.findById(id)).thenReturn(Optional.empty());
            CategoryEntity newEntity = CategoryEntity.fromDomain(category);
            when(springDataRepository.save(any(CategoryEntity.class))).thenReturn(newEntity);

            Category saved = repository.save(category);

            assertNotNull(saved);
            assertEquals(category.getName(), saved.getName());
            verify(springDataRepository).findById(id);
            verify(springDataRepository).save(entityCaptor.capture());
            assertEquals(id, entityCaptor.getValue().getId());
        }
    }

    @Nested
    @DisplayName("FindById")
    class FindById {

        @Test
        void should_ReturnDomainObject_When_EntityFound() {
            UUID id = UUID.randomUUID();
            CategoryEntity entity = CategoryEntity.fromDomain(
                    Category.reconstitute(id, "Electronics", "Electronic items", true,
                            LocalDateTime.now(), LocalDateTime.now())
            );

            when(springDataRepository.findById(id)).thenReturn(Optional.of(entity));

            Optional<Category> result = repository.findById(id);

            assertTrue(result.isPresent());
            assertEquals(id, result.get().getId());
            assertEquals("Electronics", result.get().getName());
            verify(springDataRepository).findById(id);
        }

        @Test
        void should_ReturnEmpty_When_EntityNotFound() {
            UUID id = UUID.randomUUID();
            when(springDataRepository.findById(id)).thenReturn(Optional.empty());

            Optional<Category> result = repository.findById(id);

            assertTrue(result.isEmpty());
            verify(springDataRepository).findById(id);
        }
    }

    @Nested
    @DisplayName("ExistsByName")
    class ExistsByName {

        @Test
        void should_ReturnTrue_When_NameExists() {
            when(springDataRepository.existsByName("Electronics")).thenReturn(true);

            boolean result = repository.existsByName("Electronics");

            assertTrue(result);
            verify(springDataRepository).existsByName("Electronics");
        }

        @Test
        void should_ReturnFalse_When_NameDoesNotExist() {
            when(springDataRepository.existsByName("NonExistent")).thenReturn(false);

            boolean result = repository.existsByName("NonExistent");

            assertFalse(result);
            verify(springDataRepository).existsByName("NonExistent");
        }
    }

    @Nested
    @DisplayName("ExistsByNameAndIdNot")
    class ExistsByNameAndIdNot {

        @Test
        void should_ReturnTrue_When_NameExistsForDifferentId() {
            UUID id = UUID.randomUUID();
            when(springDataRepository.existsByNameAndIdNot("Electronics", id)).thenReturn(true);

            boolean result = repository.existsByNameAndIdNot("Electronics", id);

            assertTrue(result);
            verify(springDataRepository).existsByNameAndIdNot("Electronics", id);
        }

        @Test
        void should_ReturnFalse_When_NameDoesNotExistForDifferentId() {
            UUID id = UUID.randomUUID();
            when(springDataRepository.existsByNameAndIdNot("Electronics", id)).thenReturn(false);

            boolean result = repository.existsByNameAndIdNot("Electronics", id);

            assertFalse(result);
            verify(springDataRepository).existsByNameAndIdNot("Electronics", id);
        }
    }

    @Nested
    @DisplayName("CountActiveProductsByCategory")
    class CountActiveProductsByCategory {

        @Test
        void should_DelegateToSpringDataRepository() {
            UUID categoryId = UUID.randomUUID();
            when(springDataRepository.countActiveProductsByCategory(categoryId)).thenReturn(5L);

            long result = repository.countActiveProductsByCategory(categoryId);

            assertEquals(5L, result);
            verify(springDataRepository).countActiveProductsByCategory(categoryId);
        }

        @Test
        void should_ReturnZero_When_NoActiveProducts() {
            UUID categoryId = UUID.randomUUID();
            when(springDataRepository.countActiveProductsByCategory(categoryId)).thenReturn(0L);

            long result = repository.countActiveProductsByCategory(categoryId);

            assertEquals(0L, result);
            verify(springDataRepository).countActiveProductsByCategory(categoryId);
        }
    }

    @Nested
    @DisplayName("FindAllWithFilters")
    class FindAllWithFilters {

        @Test
        void should_ReturnPageResponse_When_EntitiesFound() {
            UUID id = UUID.randomUUID();
            CategoryEntity entity = CategoryEntity.fromDomain(
                    Category.reconstitute(id, "Electronics", "Electronic items", true,
                            LocalDateTime.now(), LocalDateTime.now())
            );

            Page<CategoryEntity> entityPage = new PageImpl<>(List.of(entity));
            when(springDataRepository.findAllWithFilters(
                    anyString(), any(), any(PageRequest.class)))
                    .thenReturn(entityPage);

            PageResponse<CategorySummary> result = repository.findAllWithFilters(
                    "Electr", true, 0, 10, "name", "asc");

            assertNotNull(result);
            assertEquals(1, result.totalElements());
            assertEquals(1, result.content().size());
            assertEquals("Electronics", result.content().get(0).category().getName());
            assertEquals("Electronic items", result.content().get(0).category().getDescription());
            verify(springDataRepository).findAllWithFilters(eq("Electr"), eq(true), any(PageRequest.class));
        }

        @Test
        void should_ReturnEmptyPageResponse_When_NoEntities() {
            Page<CategoryEntity> entityPage = new PageImpl<>(List.of());
            when(springDataRepository.findAllWithFilters(
                    any(), any(), any(PageRequest.class)))
                    .thenReturn(entityPage);

            PageResponse<CategorySummary> result = repository.findAllWithFilters(
                    null, null, 0, 10, "name", "asc");

            assertNotNull(result);
            assertTrue(result.empty());
            assertEquals(0, result.totalElements());
            assertTrue(result.content().isEmpty());
            verify(springDataRepository).findAllWithFilters(
                    isNull(), isNull(), any(PageRequest.class));
        }

        @Test
        void should_DefaultToNameSort_When_InvalidOrderBy() {
            UUID id = UUID.randomUUID();
            CategoryEntity entity = CategoryEntity.fromDomain(
                    Category.reconstitute(id, "Electronics", "Electronic items", true,
                            LocalDateTime.now(), LocalDateTime.now())
            );

            Page<CategoryEntity> entityPage = new PageImpl<>(List.of(entity));
            when(springDataRepository.findAllWithFilters(
                    anyString(), any(), any(PageRequest.class)))
                    .thenReturn(entityPage);

            PageResponse<CategorySummary> result = repository.findAllWithFilters(
                    "Electr", true, 0, 10, "invalidField", "asc");

            assertNotNull(result);
            assertEquals(1, result.totalElements());
            verify(springDataRepository).findAllWithFilters(
                    eq("Electr"), eq(true), pageRequestCaptor.capture());
            PageRequest captured = pageRequestCaptor.getValue();
            assertEquals(Sort.by("name").ascending(), captured.getSort());
        }

        @Test
        void should_UseDescendingSort_When_DescOrderDir() {
            UUID id = UUID.randomUUID();
            CategoryEntity entity = CategoryEntity.fromDomain(
                    Category.reconstitute(id, "Electronics", "Electronic items", true,
                            LocalDateTime.now(), LocalDateTime.now())
            );

            Page<CategoryEntity> entityPage = new PageImpl<>(List.of(entity));
            when(springDataRepository.findAllWithFilters(
                    anyString(), any(), any(PageRequest.class)))
                    .thenReturn(entityPage);

            PageResponse<CategorySummary> result = repository.findAllWithFilters(
                    "Electr", true, 0, 10, "name", "desc");

            assertNotNull(result);
            assertEquals(1, result.totalElements());
            verify(springDataRepository).findAllWithFilters(
                    eq("Electr"), eq(true), pageRequestCaptor.capture());
            PageRequest captured = pageRequestCaptor.getValue();
            assertEquals(Sort.by("name").descending(), captured.getSort());
        }

        @Test
        void should_SortByCreatedAt() {
            UUID id = UUID.randomUUID();
            CategoryEntity entity = CategoryEntity.fromDomain(
                    Category.reconstitute(id, "Electronics", "Electronic items", true,
                            LocalDateTime.now(), LocalDateTime.now())
            );

            Page<CategoryEntity> entityPage = new PageImpl<>(List.of(entity));
            when(springDataRepository.findAllWithFilters(
                    anyString(), any(), any(PageRequest.class)))
                    .thenReturn(entityPage);

            PageResponse<CategorySummary> result = repository.findAllWithFilters(
                    "Electr", true, 0, 10, "createdAt", "asc");

            assertNotNull(result);
            assertEquals(1, result.totalElements());
            verify(springDataRepository).findAllWithFilters(
                    eq("Electr"), eq(true), pageRequestCaptor.capture());
            PageRequest captured = pageRequestCaptor.getValue();
            assertEquals(Sort.by("createdAt").ascending(), captured.getSort());
        }

        @Test
        void should_SortByUpdatedAt() {
            UUID id = UUID.randomUUID();
            CategoryEntity entity = CategoryEntity.fromDomain(
                    Category.reconstitute(id, "Electronics", "Electronic items", true,
                            LocalDateTime.now(), LocalDateTime.now())
            );

            Page<CategoryEntity> entityPage = new PageImpl<>(List.of(entity));
            when(springDataRepository.findAllWithFilters(
                    anyString(), any(), any(PageRequest.class)))
                    .thenReturn(entityPage);

            PageResponse<CategorySummary> result = repository.findAllWithFilters(
                    "Electr", true, 0, 10, "updatedAt", "asc");

            assertNotNull(result);
            assertEquals(1, result.totalElements());
            verify(springDataRepository).findAllWithFilters(
                    eq("Electr"), eq(true), pageRequestCaptor.capture());
            PageRequest captured = pageRequestCaptor.getValue();
            assertEquals(Sort.by("updatedAt").ascending(), captured.getSort());
        }
    }
}
