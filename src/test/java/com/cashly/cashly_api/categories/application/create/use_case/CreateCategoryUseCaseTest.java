package com.cashly.cashly_api.categories.application.create.use_case;

import com.cashly.cashly_api.categories.application.create.request.CreateCategoryRequest;
import com.cashly.cashly_api.categories.application.dto.response.CategoryDetailResponse;
import com.cashly.cashly_api.categories.application.exceptions.DuplicateCategoryNameException;
import com.cashly.cashly_api.categories.application.mappers.CategoryMapper;
import com.cashly.cashly_api.categories.domain.entity.Category;
import com.cashly.cashly_api.categories.domain.repository.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateCategoryUseCase Tests")
class CreateCategoryUseCaseTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CreateCategoryUseCase useCase;

    @Test
    void should_CreateCategory_When_ValidRequest() {
        CreateCategoryRequest request = new CreateCategoryRequest("New Category", "Description");

        Category savedCategory = Category.reconstitute(
                UUID.randomUUID(), "New Category", "Description", true,
                LocalDateTime.now(), LocalDateTime.now());

        CategoryDetailResponse expectedResponse = new CategoryDetailResponse(
                savedCategory.getId().toString(), "New Category", "Description", true, 0L,
                savedCategory.getCreatedAt(), savedCategory.getUpdatedAt());

        when(categoryRepository.existsByName("New Category")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);
        when(categoryMapper.toDetailResponse(savedCategory, 0L)).thenReturn(expectedResponse);

        CategoryDetailResponse result = useCase.execute(request);

        assertNotNull(result);
        assertEquals("New Category", result.name());
        assertEquals(0L, result.productCount());
        verify(categoryRepository).existsByName("New Category");
        verify(categoryRepository).save(any(Category.class));
        verify(categoryMapper).toDetailResponse(savedCategory, 0L);
    }

    @Test
    void should_ThrowDuplicateException_When_NameAlreadyExists() {
        CreateCategoryRequest request = new CreateCategoryRequest("Existing", "Description");

        when(categoryRepository.existsByName("Existing")).thenReturn(true);

        assertThrows(DuplicateCategoryNameException.class, () -> useCase.execute(request));

        verify(categoryRepository).existsByName("Existing");
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void should_TrimName_When_CheckingDuplicatesAndCreating() {
        CreateCategoryRequest request = new CreateCategoryRequest("  Trimmed  ", "Description");

        Category savedCategory = Category.reconstitute(
                UUID.randomUUID(), "Trimmed", "Description", true,
                LocalDateTime.now(), LocalDateTime.now());

        CategoryDetailResponse expectedResponse = new CategoryDetailResponse(
                savedCategory.getId().toString(), "Trimmed", "Description", true, 0L,
                savedCategory.getCreatedAt(), savedCategory.getUpdatedAt());

        when(categoryRepository.existsByName("Trimmed")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);
        when(categoryMapper.toDetailResponse(savedCategory, 0L)).thenReturn(expectedResponse);

        CategoryDetailResponse result = useCase.execute(request);

        assertNotNull(result);
        verify(categoryRepository).existsByName("Trimmed");
    }
}
