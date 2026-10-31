package com.cashly.cashly_api.catalog.categories.application.queries.use_case;

import com.cashly.cashly_api.catalog.categories.application.dto.response.CategoryDetailResponse;
import com.cashly.cashly_api.catalog.categories.application.exceptions.CategoryNotFoundException;
import com.cashly.cashly_api.catalog.categories.application.mappers.CategoryMapper;
import com.cashly.cashly_api.catalog.categories.domain.entity.Category;
import com.cashly.cashly_api.catalog.categories.application.ports.out.CategoryUsageGateway;
import com.cashly.cashly_api.catalog.categories.domain.repository.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetCategoryUseCase Tests")
class GetCategoryUseCaseTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryUsageGateway usageGateway;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private GetCategoryUseCase useCase;

    @Test
    void should_ReturnDetailWithProductCount_When_CategoryExists() {
        UUID id = UUID.randomUUID();
        Category category = Category.reconstitute(id, "Test Category",
                "Description", true, LocalDateTime.now(), LocalDateTime.now());

        CategoryDetailResponse expectedResponse = new CategoryDetailResponse(
                id.toString(), "Test Category", "Description", true, 5L,
                category.getCreatedAt(), category.getUpdatedAt());

        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));
        when(usageGateway.countActiveProductsByCategory(id)).thenReturn(5L);
        when(categoryMapper.toDetailResponse(category, 5L))
                .thenReturn(expectedResponse);

        CategoryDetailResponse result = useCase.execute(id);

        assertNotNull(result);
        assertEquals("Test Category", result.name());
        assertEquals(5L, result.productCount());
        verify(categoryRepository).findById(id);
        verify(usageGateway).countActiveProductsByCategory(id);
        verify(categoryMapper).toDetailResponse(category, 5L);
    }

    @Test
    void should_ThrowNotFoundException_When_CategoryDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class,
                () -> useCase.execute(id));

        verify(categoryRepository).findById(id);
        verify(usageGateway, never()).countActiveProductsByCategory(any());
    }
}
