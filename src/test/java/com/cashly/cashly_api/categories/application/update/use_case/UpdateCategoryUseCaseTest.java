package com.cashly.cashly_api.categories.application.update.use_case;

import com.cashly.cashly_api.categories.application.update.request.UpdateCategoryRequest;
import com.cashly.cashly_api.categories.application.dto.response.CategoryDetailResponse;
import com.cashly.cashly_api.categories.application.exceptions.CategoryNotFoundException;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateCategoryUseCase Tests")
class UpdateCategoryUseCaseTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private UpdateCategoryUseCase useCase;

    @Test
    void should_UpdateCategory_When_ValidRequest() {
        UUID id = UUID.randomUUID();
        UpdateCategoryRequest request = new UpdateCategoryRequest("Updated Name", "Updated Desc", true);

        Category existing = Category.reconstitute(
                id, "Old Name", "Old Desc", true,
                LocalDateTime.now(), LocalDateTime.now());

        Category saved = Category.reconstitute(
                id, "Updated Name", "Updated Desc", true,
                existing.getCreatedAt(), LocalDateTime.now());

        CategoryDetailResponse expectedResponse = new CategoryDetailResponse(
                id.toString(), "Updated Name", "Updated Desc", true, 0L,
                saved.getCreatedAt(), saved.getUpdatedAt());

        when(categoryRepository.findById(id)).thenReturn(Optional.of(existing));
        when(categoryRepository.existsByNameAndIdNot("Updated Name", id)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);
        when(categoryMapper.toDetailResponse(saved, 0L)).thenReturn(expectedResponse);

        CategoryDetailResponse result = useCase.execute(id, request);

        assertNotNull(result);
        assertEquals("Updated Name", result.name());
        verify(categoryRepository).findById(id);
        verify(categoryRepository).existsByNameAndIdNot("Updated Name", id);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void should_ThrowNotFoundException_When_CategoryDoesNotExist() {
        UUID id = UUID.randomUUID();
        UpdateCategoryRequest request = new UpdateCategoryRequest("Name", "Desc", true);

        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> useCase.execute(id, request));

        verify(categoryRepository).findById(id);
        verify(categoryRepository, never()).existsByNameAndIdNot(anyString(), any());
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void should_ThrowDuplicateException_When_NameExistsForDifferentCategory() {
        UUID id = UUID.randomUUID();
        UpdateCategoryRequest request = new UpdateCategoryRequest("Duplicate Name", "Desc", true);

        Category existing = Category.reconstitute(
                id, "Old Name", "Old Desc", true,
                LocalDateTime.now(), LocalDateTime.now());

        when(categoryRepository.findById(id)).thenReturn(Optional.of(existing));
        when(categoryRepository.existsByNameAndIdNot("Duplicate Name", id)).thenReturn(true);

        assertThrows(DuplicateCategoryNameException.class, () -> useCase.execute(id, request));

        verify(categoryRepository).findById(id);
        verify(categoryRepository).existsByNameAndIdNot("Duplicate Name", id);
        verify(categoryRepository, never()).save(any());
    }
}
