package com.cashly.cashly_api.categories.application.mappers;

import com.cashly.cashly_api.categories.application.dto.response.CategoryDetailResponse;
import com.cashly.cashly_api.categories.application.dto.response.CategoryListResponse;
import com.cashly.cashly_api.categories.domain.entities.Category;
import com.cashly.cashly_api.categories.domain.repositories.CategoryRepository.CategorySummary;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CategoryMapper Unit Tests")
class CategoryMapperTest {

    private final CategoryMapper mapper = new CategoryMapper();

    private Category createCategory(String name, String description, boolean active) {
        return Category.reconstitute(
                UUID.randomUUID(), name, description, active,
                LocalDateTime.of(2026, 1, 15, 10, 0),
                LocalDateTime.of(2026, 1, 15, 10, 0)
        );
    }

    @Nested
    @DisplayName("toListResponse")
    class ToListResponse {

        @Test
        void should_MapToCategoryListResponse() {
            Category category = createCategory("Electrónica", "Productos electrónicos", true);
            CategorySummary summary = new CategorySummary(category, 0L);

            CategoryListResponse response = mapper.toListResponse(summary);

            assertEquals(category.getId().toString(), response.id());
            assertEquals("Electrónica", response.name());
            assertEquals("Productos electrónicos", response.description());
            assertTrue(response.active());
        }

        @Test
        void should_MapInactiveCategory() {
            Category category = createCategory("Obsoleto", "Categoría obsoleta", false);
            CategorySummary summary = new CategorySummary(category, 0L);

            CategoryListResponse response = mapper.toListResponse(summary);

            assertEquals(category.getId().toString(), response.id());
            assertFalse(response.active());
        }

        @Test
        void should_MapWithNullDescription() {
            Category category = createCategory("Test", null, true);
            CategorySummary summary = new CategorySummary(category, 0L);

            CategoryListResponse response = mapper.toListResponse(summary);

            assertNull(response.description());
        }
    }

    @Nested
    @DisplayName("toDetailResponse")
    class ToDetailResponse {

        @Test
        void should_MapToCategoryDetailResponse() {
            Category category = createCategory("Ropa", "Ropa y accesorios", true);

            CategoryDetailResponse response = mapper.toDetailResponse(category, 5L);

            assertEquals(category.getId().toString(), response.id());
            assertEquals("Ropa", response.name());
            assertEquals("Ropa y accesorios", response.description());
            assertTrue(response.active());
            assertEquals(5L, response.productCount());
            assertNotNull(response.createdAt());
            assertNotNull(response.updatedAt());
        }

        @Test
        void should_MapWithZeroProductCount() {
            Category category = createCategory("Nueva", "Nueva categoría", true);

            CategoryDetailResponse response = mapper.toDetailResponse(category, 0L);

            assertEquals(0L, response.productCount());
        }

        @Test
        void should_IncludeTimestamps() {
            LocalDateTime createdAt = LocalDateTime.of(2026, 3, 10, 14, 30);
            LocalDateTime updatedAt = LocalDateTime.of(2026, 3, 11, 9, 15);
            Category category = Category.reconstitute(
                    UUID.randomUUID(), "Test", "Desc", true,
                    createdAt, updatedAt
            );

            CategoryDetailResponse response = mapper.toDetailResponse(category, 0L);

            assertEquals(createdAt, response.createdAt());
            assertEquals(updatedAt, response.updatedAt());
        }
    }
}
