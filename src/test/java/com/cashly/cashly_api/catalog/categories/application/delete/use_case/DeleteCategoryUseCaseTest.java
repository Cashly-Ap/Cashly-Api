package com.cashly.cashly_api.catalog.categories.application.delete.use_case;

import com.cashly.cashly_api.catalog.categories.application.exceptions.CategoryHasActiveProductsException;
import com.cashly.cashly_api.catalog.categories.application.exceptions.CategoryNotFoundException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteCategoryUseCase Tests")
class DeleteCategoryUseCaseTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryUsageGateway usageGateway;

    @InjectMocks
    private DeleteCategoryUseCase useCase;

    @Test
    void should_DeactivateCategory_When_NoActiveProducts() {
        UUID id = UUID.randomUUID();
        Category category = Category.reconstitute(id, "Category", "Description",
                true, LocalDateTime.now(), LocalDateTime.now());

        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));
        when(usageGateway.countActiveProductsByCategory(id)).thenReturn(0L);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        useCase.execute(id);

        assertFalse(category.isActive());
        verify(categoryRepository).findById(id);
        verify(usageGateway).countActiveProductsByCategory(id);
        verify(categoryRepository).save(category);
    }

    @Test
    void should_ThrowNotFoundException_When_CategoryDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class,
                () -> useCase.execute(id));

        verify(categoryRepository).findById(id);
        verify(usageGateway, never()).countActiveProductsByCategory(any());
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void should_ThrowHasActiveProductsException_When_CategoryHasActiveProducts() {
        UUID id = UUID.randomUUID();
        Category category = Category.reconstitute(id, "Category", "Description",
                true, LocalDateTime.now(), LocalDateTime.now());

        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));
        when(usageGateway.countActiveProductsByCategory(id)).thenReturn(3L);

        assertThrows(CategoryHasActiveProductsException.class,
                () -> useCase.execute(id));

        verify(categoryRepository).findById(id);
        verify(usageGateway).countActiveProductsByCategory(id);
        verify(categoryRepository, never()).save(any());
    }
}
