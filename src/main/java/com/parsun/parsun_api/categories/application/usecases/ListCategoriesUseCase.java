package com.parsun.parsun_api.categories.application.usecases;

import com.parsun.parsun_api.categories.application.dto.response.CategoryListResponse;
import com.parsun.parsun_api.categories.application.mappers.CategoryMapper;
import com.parsun.parsun_api.categories.domain.repositories.CategoryRepository;
import com.parsun.parsun_api.categories.domain.repositories.CategoryRepository.CategorySummary;
import com.parsun.parsun_api.shared.dto.PageResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class ListCategoriesUseCase {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public ListCategoriesUseCase(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<CategoryListResponse> execute(String search, Boolean active,
                                                       int page, int size,
                                                       String orderBy, String orderDir) {
        PageResponse<CategorySummary> summaryPage = categoryRepository.findAllWithFilters(
                search, active, page, size, orderBy, orderDir);

        List<CategoryListResponse> content = summaryPage.content().stream()
                .map(categoryMapper::toListResponse)
                .toList();

        return new PageResponse<>(content, summaryPage.totalElements(), summaryPage.totalPages(),
                summaryPage.size(), summaryPage.number(), summaryPage.first(),
                summaryPage.last(), summaryPage.empty());
    }
}
