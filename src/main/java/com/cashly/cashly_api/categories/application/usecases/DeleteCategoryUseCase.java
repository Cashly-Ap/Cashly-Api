package com.cashly.cashly_api.categories.application.usecases;

import com.cashly.cashly_api.categories.application.exceptions.CategoryHasActiveProductsException;
import com.cashly.cashly_api.categories.application.exceptions.CategoryNotFoundException;
import com.cashly.cashly_api.categories.domain.entities.Category;
import com.cashly.cashly_api.categories.domain.repositories.CategoryRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class DeleteCategoryUseCase {

    private final CategoryRepository categoryRepository;

    public DeleteCategoryUseCase(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public void execute(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        long activeProductCount = categoryRepository.countActiveProductsByCategory(id);
        if (activeProductCount > 0) {
            throw new CategoryHasActiveProductsException(id);
        }

        category.deactivate();
        categoryRepository.save(category);
    }
}
