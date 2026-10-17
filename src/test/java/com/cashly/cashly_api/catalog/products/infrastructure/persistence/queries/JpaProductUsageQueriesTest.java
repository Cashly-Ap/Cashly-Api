package com.cashly.cashly_api.catalog.products.infrastructure.persistence.queries;

import com.cashly.cashly_api.catalog.products.infrastructure.persistence.repositories.SpringDataProductUsageRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.UUID;
import java.util.List;
import java.util.Set;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JpaProductUsageQueriesTest {
    @Mock private SpringDataProductUsageRepository springDataRepository;
    private JpaProductUsageQueries repository;
    @BeforeEach void setUp() {
        repository = new JpaProductUsageQueries(springDataRepository);
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

    @Test
    void shouldCountEveryRequestedCategoryWithOneGroupedQuery() {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        Set<UUID> ids = Set.of(first, second);
        when(springDataRepository.countActiveProductsByCategoryIds(ids))
                .thenReturn(List.of(new Object[]{first, 5L}, new Object[]{second, 2}));
        assertEquals(Map.of(first, 5L, second, 2L), repository.countActiveProductsByCategoryIds(ids));
        verify(springDataRepository).countActiveProductsByCategoryIds(ids);
        verifyNoMoreInteractions(springDataRepository);
    }

    @Test
    void shouldResolvePriceTypesInUseWithOneBatchQuery() {
        UUID used = UUID.randomUUID();
        UUID unused = UUID.randomUUID();
        Set<UUID> ids = Set.of(used, unused);
        when(springDataRepository.findIdsInUseByProducts(ids)).thenReturn(Set.of(used));
        assertEquals(Set.of(used), repository.findIdsInUseByProducts(ids));
        verify(springDataRepository).findIdsInUseByProducts(ids);
        verifyNoMoreInteractions(springDataRepository);
    }

    @Test
    void shouldSkipUsageQueriesForEmptyInput() {
        assertEquals(Map.of(), repository.countActiveProductsByCategoryIds(Set.of()));
        assertEquals(Set.of(), repository.findIdsInUseByProducts(Set.of()));
        verifyNoInteractions(springDataRepository);
    }
}