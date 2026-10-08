package com.cashly.cashly_api.categories.application.usecases;

import com.cashly.cashly_api.categories.application.dto.response.CategoryDetailResponse;
import com.cashly.cashly_api.categories.application.exceptions.CategoryNotFoundException;
import com.cashly.cashly_api.categories.application.mappers.CategoryMapper;
import com.cashly.cashly_api.categories.domain.entities.Category;
import com.cashly.cashly_api.categories.application.contracts.CategoryUsageGateway;
import com.cashly.cashly_api.categories.domain.repositories.CategoryRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class GetCategoryUseCase {

    private final CategoryUsageGateway usageGateway;
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public GetCategoryUseCase(CategoryUsageGateway usageGateway, CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.usageGateway = usageGateway;
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Transactional(readOnly = true)
    public CategoryDetailResponse execute(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
        long productCount = usageGateway.countActiveProductsByCategory(id);
        return categoryMapper.toDetailResponse(category, productCount);
    }
}
