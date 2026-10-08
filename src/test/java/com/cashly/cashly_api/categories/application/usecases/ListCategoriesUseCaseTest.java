package com.cashly.cashly_api.categories.application.usecases;

import com.cashly.cashly_api.categories.application.dto.response.CategoryListResponse;
import com.cashly.cashly_api.categories.application.mappers.CategoryMapper;
import com.cashly.cashly_api.categories.domain.entities.Category;
import com.cashly.cashly_api.categories.application.contracts.CategoryUsageGateway;
import com.cashly.cashly_api.categories.domain.repositories.CategoryRepository;
import com.cashly.cashly_api.categories.application.dto.response.CategorySummary;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListCategoriesUseCase Tests")
class ListCategoriesUseCaseTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock private CategoryUsageGateway usageGateway;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private ListCategoriesUseCase useCase;

    @Test
    void should_ReturnPaginatedList_When_CategoriesExist() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        Category cat1 = Category.reconstitute(id1, "Cat 1", "Desc 1", true, LocalDateTime.now(), LocalDateTime.now());
        Category cat2 = Category.reconstitute(id2, "Cat 2", "Desc 2", true, LocalDateTime.now(), LocalDateTime.now());

        CategorySummary summary1 = new CategorySummary(cat1, 3L);
        CategorySummary summary2 = new CategorySummary(cat2, 1L);

        CategoryListResponse resp1 = new CategoryListResponse(id1.toString(), "Cat 1", "Desc 1", true, false);
        CategoryListResponse resp2 = new CategoryListResponse(id2.toString(), "Cat 2", "Desc 2", true, false);

        PageResponse<Category> summaryPage = new PageResponse<>(
                List.of(cat1, cat2), 2L, 1, 10, 0, true, true, false);

        when(categoryRepository.findAllWithFilters(null, null, 0, 10, "name", "asc"))
                .thenReturn(summaryPage);
        when(usageGateway.countActiveProductsByCategoryIds(java.util.Set.of(id1, id2))).thenReturn(java.util.Map.of(id1, 3L, id2, 1L));
        when(categoryMapper.toListResponse(summary1)).thenReturn(resp1);
        when(categoryMapper.toListResponse(summary2)).thenReturn(resp2);

        PageResponse<CategoryListResponse> result = useCase.execute(null, null, 0, 10, "name", "asc");

        assertNotNull(result);
        assertEquals(2, result.content().size());
        assertEquals(2L, result.totalElements());
        assertEquals("Cat 1", result.content().get(0).name());
        assertEquals("Cat 2", result.content().get(1).name());
        verify(categoryRepository).findAllWithFilters(null, null, 0, 10, "name", "asc");
    }

    @Test
    void should_PassFiltersToRepository_When_SearchAndActiveProvided() {
        PageResponse<Category> emptyPage = new PageResponse<>(
                List.of(), 0L, 0, 10, 0, true, true, true);

        when(categoryRepository.findAllWithFilters("test", true, 0, 10, "name", "asc"))
                .thenReturn(emptyPage);

        PageResponse<CategoryListResponse> result = useCase.execute("test", true, 0, 10, "name", "asc");

        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        verify(categoryRepository).findAllWithFilters("test", true, 0, 10, "name", "asc");
    }

    @Test
    void should_ReturnEmptyPage_When_NoCategoriesFound() {
        PageResponse<Category> emptyPage = new PageResponse<>(
                List.of(), 0L, 0, 10, 0, true, true, true);

        when(categoryRepository.findAllWithFilters(null, null, 0, 10, "name", "asc"))
                .thenReturn(emptyPage);

        PageResponse<CategoryListResponse> result = useCase.execute(null, null, 0, 10, "name", "asc");

        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        assertEquals(0L, result.totalElements());
        assertTrue(result.empty());
    }
    @Test
    void shouldPreserveDeletabilityFromOneBatchCountAndDefaultMissingCountsToZero() {
        UUID usedId = UUID.randomUUID();
        UUID unusedId = UUID.randomUUID();
        Category used = Category.reconstitute(usedId, "Used", "", true, LocalDateTime.now(), LocalDateTime.now());
        Category unused = Category.reconstitute(unusedId, "Unused", "", true, LocalDateTime.now(), LocalDateTime.now());
        when(categoryRepository.findAllWithFilters(null, null, 0, 10, "name", "asc"))
                .thenReturn(new PageResponse<>(List.of(used, unused), 2L, 1, 10, 0, true, true, false));
        when(usageGateway.countActiveProductsByCategoryIds(java.util.Set.of(usedId, unusedId)))
                .thenReturn(java.util.Map.of(usedId, 3L));
        var result = new ListCategoriesUseCase(usageGateway, categoryRepository, new CategoryMapper())
                .execute(null, null, 0, 10, "name", "asc");
        assertFalse(result.content().get(0).deletable());
        assertTrue(result.content().get(1).deletable());
        verify(usageGateway).countActiveProductsByCategoryIds(java.util.Set.of(usedId, unusedId));
        verifyNoMoreInteractions(usageGateway);
    }
}