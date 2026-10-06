package com.cashly.cashly_api.categories.application.usecases;

import com.cashly.cashly_api.categories.application.dto.request.UpdateCategoryRequest;
import com.cashly.cashly_api.categories.application.dto.response.CategoryDetailResponse;
import com.cashly.cashly_api.categories.application.exceptions.CategoryNotFoundException;
import com.cashly.cashly_api.categories.application.exceptions.DuplicateCategoryNameException;
import com.cashly.cashly_api.categories.application.mappers.CategoryMapper;
import com.cashly.cashly_api.categories.domain.entities.Category;
import com.cashly.cashly_api.categories.domain.repositories.CategoryRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class UpdateCategoryUseCase {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public UpdateCategoryUseCase(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Transactional
    public CategoryDetailResponse execute(UUID id, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        if (categoryRepository.existsByNameAndIdNot(request.name().trim(), id)) {
            throw new DuplicateCategoryNameException(request.name());
        }

        category.update(request.name().trim(), request.description(), request.active());
        Category saved = categoryRepository.save(category);
        return categoryMapper.toDetailResponse(saved, 0L);
    }
}
