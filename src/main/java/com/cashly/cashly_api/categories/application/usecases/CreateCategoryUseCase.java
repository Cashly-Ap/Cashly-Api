package com.cashly.cashly_api.categories.application.usecases;

import com.cashly.cashly_api.categories.application.dto.request.CreateCategoryRequest;
import com.cashly.cashly_api.categories.application.dto.response.CategoryDetailResponse;
import com.cashly.cashly_api.categories.application.exceptions.DuplicateCategoryNameException;
import com.cashly.cashly_api.categories.application.mappers.CategoryMapper;
import com.cashly.cashly_api.categories.domain.entities.Category;
import com.cashly.cashly_api.categories.domain.repositories.CategoryRepository;
import org.springframework.transaction.annotation.Transactional;

public class CreateCategoryUseCase {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CreateCategoryUseCase(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Transactional
    public CategoryDetailResponse execute(CreateCategoryRequest request) {
        if (categoryRepository.existsByName(request.name().trim())) {
            throw new DuplicateCategoryNameException(request.name());
        }
        Category category = Category.create(request.name().trim(), request.description());
        Category saved = categoryRepository.save(category);
        return categoryMapper.toDetailResponse(saved, 0L);
    }
}
