package com.cashly.cashly_api.catalog.categories.application.mappers;

import com.cashly.cashly_api.catalog.categories.application.dto.response.CategoryDetailResponse;
import com.cashly.cashly_api.catalog.categories.application.dto.response.CategoryListResponse;
import com.cashly.cashly_api.catalog.categories.domain.entity.Category;
import com.cashly.cashly_api.catalog.categories.application.dto.response.CategorySummary;

public class CategoryMapper {

    public CategoryListResponse toListResponse(CategorySummary summary) {
        Category c = summary.category();
        return new CategoryListResponse(c.getId().toString(), c.getName(),
                c.getDescription(), c.isActive(), summary.productCount() == 0);
    }

    public CategoryDetailResponse toDetailResponse(Category category,
            long productCount) {
        return new CategoryDetailResponse(category.getId().toString(),
                category.getName(), category.getDescription(),
                category.isActive(), productCount, category.getCreatedAt(),
                category.getUpdatedAt());
    }
}
