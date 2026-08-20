package com.parsun.parsun_api.price_types.infrastructure.persistence.repositories;

import com.parsun.parsun_api.price_types.domain.entities.PriceType;
import com.parsun.parsun_api.price_types.infrastructure.persistence.entities.PriceTypeEntity;
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
@DisplayName("JpaPriceTypeRepository Unit Tests")
class JpaPriceTypeRepositoryTest {

    @Mock
    private SpringDataPriceTypeRepository springDataRepository;

    @Captor
    private ArgumentCaptor<PriceTypeEntity> entityCaptor;

    @Captor
    private ArgumentCaptor<PageRequest> pageRequestCaptor;

    private JpaPriceTypeRepository repository;

    @BeforeEach
    void setUp() {
        repository = new JpaPriceTypeRepository(springDataRepository);
    }

    @Nested
    @DisplayName("Save")
    class Save {

        @Test
        void should_CreateNewEntity_When_IdIsNull() {
            PriceType priceType = PriceType.create("Standard", "Standard pricing");
            UUID generatedId = UUID.randomUUID();

            PriceTypeEntity entityWithoutId = PriceTypeEntity.fromDomain(priceType);
            entityWithoutId.setId(generatedId);

            when(springDataRepository.save(any(PriceTypeEntity.class))).thenReturn(entityWithoutId);

            PriceType saved = repository.save(priceType);

            assertNotNull(saved);
            assertEquals(priceType.getName(), saved.getName());
            assertEquals(priceType.getDescription(), saved.getDescription());
            assertTrue(saved.isActive());

            verify(springDataRepository).save(entityCaptor.capture());
            PriceTypeEntity captured = entityCaptor.getValue();
            assertNotNull(captured.getId());
            assertEquals(priceType.getName(), captured.getName());
            assertEquals(priceType.getDescription(), captured.getDescription());
            assertTrue(captured.isActive());
        }

        @Test
        void should_UpdateExistingEntity_When_IdIsNotNullAndFound() {
            UUID id = UUID.randomUUID();
            PriceType existing = PriceType.reconstitute(
                    id, "Old Name", "Old Description", true,
                    LocalDateTime.now().minusDays(1), LocalDateTime.now().minusDays(1)
            );
            existing.update("Updated Name", "Updated Description", true);

            PriceTypeEntity existingEntity = PriceTypeEntity.fromDomain(existing);
            when(springDataRepository.findById(id)).thenReturn(Optional.of(existingEntity));
            when(springDataRepository.save(any(PriceTypeEntity.class))).thenReturn(existingEntity);

            PriceType saved = repository.save(existing);

            assertNotNull(saved);
            assertEquals("Updated Name", saved.getName());
            assertEquals("Updated Description", saved.getDescription());
            verify(springDataRepository).findById(id);
            verify(springDataRepository).save(existingEntity);
        }

        @Test
        void should_CreateNewEntity_When_IdIsNotNullButNotFound() {
            UUID id = UUID.randomUUID();
            PriceType priceType = PriceType.reconstitute(
                    id, "New Name", "New Description", true,
                    LocalDateTime.now(), LocalDateTime.now()
            );

            when(springDataRepository.findById(id)).thenReturn(Optional.empty());
            PriceTypeEntity newEntity = PriceTypeEntity.fromDomain(priceType);
            when(springDataRepository.save(any(PriceTypeEntity.class))).thenReturn(newEntity);

            PriceType saved = repository.save(priceType);

            assertNotNull(saved);
            assertEquals(priceType.getName(), saved.getName());
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
            PriceTypeEntity entity = PriceTypeEntity.fromDomain(
                    PriceType.reconstitute(id, "Standard", "Standard pricing", true,
                            LocalDateTime.now(), LocalDateTime.now())
            );

            when(springDataRepository.findById(id)).thenReturn(Optional.of(entity));

            Optional<PriceType> result = repository.findById(id);

            assertTrue(result.isPresent());
            assertEquals(id, result.get().getId());
            assertEquals("Standard", result.get().getName());
            verify(springDataRepository).findById(id);
        }

        @Test
        void should_ReturnEmpty_When_EntityNotFound() {
            UUID id = UUID.randomUUID();
            when(springDataRepository.findById(id)).thenReturn(Optional.empty());

            Optional<PriceType> result = repository.findById(id);

            assertTrue(result.isEmpty());
            verify(springDataRepository).findById(id);
        }
    }

    @Nested
    @DisplayName("FindAllWithFilters")
    class FindAllWithFilters {

        @Test
        void should_ReturnPageResponse_When_EntitiesFound() {
            PriceTypeEntity entity = PriceTypeEntity.fromDomain(
                    PriceType.reconstitute(UUID.randomUUID(), "Standard", "Standard pricing", true,
                            LocalDateTime.now(), LocalDateTime.now())
            );
            Page<PriceTypeEntity> entityPage = new PageImpl<>(List.of(entity));
            when(springDataRepository.findAllWithFilters(anyString(), any(), any(PageRequest.class)))
                    .thenReturn(entityPage);

            PageResponse<PriceType> result = repository.findAllWithFilters(
                    "Stand", true, 0, 10, "name", "asc");

            assertNotNull(result);
            assertEquals(1, result.totalElements());
            assertEquals(1, result.content().size());
            assertEquals("Standard", result.content().get(0).getName());
            verify(springDataRepository).findAllWithFilters(eq("Stand"), eq(true), any(PageRequest.class));
        }

        @Test
        void should_ReturnEmptyPageResponse_When_NoEntities() {
            Page<PriceTypeEntity> entityPage = new PageImpl<>(List.of());
            when(springDataRepository.findAllWithFilters(any(), any(), any(PageRequest.class)))
                    .thenReturn(entityPage);

            PageResponse<PriceType> result = repository.findAllWithFilters(
                    null, null, 0, 10, "name", "asc");

            assertNotNull(result);
            assertTrue(result.empty());
            assertEquals(0, result.totalElements());
            assertTrue(result.content().isEmpty());
            verify(springDataRepository).findAllWithFilters(isNull(), isNull(), any(PageRequest.class));
        }

        @Test
        void should_DefaultToCreatedAtSort_When_OrderByNotAllowed() {
            Page<PriceTypeEntity> entityPage = new PageImpl<>(List.of());
            when(springDataRepository.findAllWithFilters(any(), any(), any(PageRequest.class)))
                    .thenReturn(entityPage);

            repository.findAllWithFilters(null, null, 0, 10, "invalid_field", "asc");

            verify(springDataRepository).findAllWithFilters(isNull(), isNull(), pageRequestCaptor.capture());
            assertEquals(Sort.by("createdAt").ascending(), pageRequestCaptor.getValue().getSort());
        }

        @Test
        void should_UseDescendingSort_When_OrderDirIsDesc() {
            Page<PriceTypeEntity> entityPage = new PageImpl<>(List.of());
            when(springDataRepository.findAllWithFilters(any(), any(), any(PageRequest.class)))
                    .thenReturn(entityPage);

            repository.findAllWithFilters(null, null, 0, 10, "name", "desc");

            verify(springDataRepository).findAllWithFilters(isNull(), isNull(), pageRequestCaptor.capture());
            assertEquals(Sort.by("name").descending(), pageRequestCaptor.getValue().getSort());
        }
    }

    @Nested
    @DisplayName("ExistsById")
    class ExistsById {

        @Test
        void should_ReturnTrue_When_EntityExists() {
            UUID id = UUID.randomUUID();
            when(springDataRepository.existsById(id)).thenReturn(true);

            boolean result = repository.existsById(id);

            assertTrue(result);
            verify(springDataRepository).existsById(id);
        }

        @Test
        void should_ReturnFalse_When_EntityDoesNotExist() {
            UUID id = UUID.randomUUID();
            when(springDataRepository.existsById(id)).thenReturn(false);

            boolean result = repository.existsById(id);

            assertFalse(result);
            verify(springDataRepository).existsById(id);
        }
    }

    @Nested
    @DisplayName("ExistsByName")
    class ExistsByName {

        @Test
        void should_ReturnTrue_When_NameExists() {
            when(springDataRepository.existsByName("Standard")).thenReturn(true);

            boolean result = repository.existsByName("Standard");

            assertTrue(result);
            verify(springDataRepository).existsByName("Standard");
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
            when(springDataRepository.existsByNameAndIdNot("Standard", id)).thenReturn(true);

            boolean result = repository.existsByNameAndIdNot("Standard", id);

            assertTrue(result);
            verify(springDataRepository).existsByNameAndIdNot("Standard", id);
        }

        @Test
        void should_ReturnFalse_When_NameDoesNotExistForDifferentId() {
            UUID id = UUID.randomUUID();
            when(springDataRepository.existsByNameAndIdNot("Standard", id)).thenReturn(false);

            boolean result = repository.existsByNameAndIdNot("Standard", id);

            assertFalse(result);
            verify(springDataRepository).existsByNameAndIdNot("Standard", id);
        }
    }

    @Nested
    @DisplayName("IsInUseByProducts")
    class IsInUseByProducts {

        @Test
        void should_ReturnTrue_When_PriceTypeIsInUse() {
            UUID id = UUID.randomUUID();
            when(springDataRepository.isInUseByProducts(id)).thenReturn(true);

            boolean result = repository.isInUseByProducts(id);

            assertTrue(result);
            verify(springDataRepository).isInUseByProducts(id);
        }

        @Test
        void should_ReturnFalse_When_PriceTypeIsNotInUse() {
            UUID id = UUID.randomUUID();
            when(springDataRepository.isInUseByProducts(id)).thenReturn(false);

            boolean result = repository.isInUseByProducts(id);

            assertFalse(result);
            verify(springDataRepository).isInUseByProducts(id);
        }
    }

    @Nested
    @DisplayName("Delete")
    class Delete {

        @Test
        void should_DelegateToSpringDataRepository() {
            UUID id = UUID.randomUUID();

            repository.delete(id);

            verify(springDataRepository).deleteById(id);
        }
    }
}
