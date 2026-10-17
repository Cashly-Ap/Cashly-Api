package com.cashly.cashly_api.catalog.categories.application.delete.use_case;

import com.cashly.cashly_api.catalog.categories.application.exceptions.CategoryHasActiveProductsException;
import com.cashly.cashly_api.catalog.categories.application.exceptions.CategoryNotFoundException;
import com.cashly.cashly_api.catalog.categories.domain.entity.Category;
import com.cashly.cashly_api.catalog.categories.application.ports.out.CategoryUsageGateway;
import com.cashly.cashly_api.catalog.categories.domain.repository.CategoryRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class DeleteCategoryUseCase {

    private final CategoryUsageGateway usageGateway;
    private final CategoryRepository categoryRepository;

    public DeleteCategoryUseCase(CategoryUsageGateway usageGateway, CategoryRepository categoryRepository) {
        this.usageGateway = usageGateway;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public void execute(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        long activeProductCount = usageGateway.countActiveProductsByCategory(id);
        if (activeProductCount > 0) {
            throw new CategoryHasActiveProductsException(id);
        }

        category.deactivate();
        categoryRepository.save(category);
    }
}
