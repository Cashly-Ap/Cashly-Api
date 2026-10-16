package com.cashly.cashly_api.categories.application.create;

import com.cashly.cashly_api.categories.application.create.CreateCategoryRequest;
import com.cashly.cashly_api.categories.application.dto.response.CategoryDetailResponse;
import com.cashly.cashly_api.categories.application.exceptions.DuplicateCategoryNameException;
import com.cashly.cashly_api.categories.application.mappers.CategoryMapper;
import com.cashly.cashly_api.categories.domain.Category;
import com.cashly.cashly_api.categories.domain.CategoryRepository;
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
