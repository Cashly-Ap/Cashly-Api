package com.cashly.cashly_api.catalog.categories.application.queries.use_case;

import com.cashly.cashly_api.catalog.categories.application.dto.response.CategoryListResponse;
import com.cashly.cashly_api.catalog.categories.application.mappers.CategoryMapper;
import com.cashly.cashly_api.catalog.categories.application.ports.out.CategoryUsageGateway;
import com.cashly.cashly_api.catalog.categories.domain.repository.CategoryRepository;
import com.cashly.cashly_api.catalog.categories.application.dto.response.CategorySummary;
import com.cashly.cashly_api.catalog.categories.domain.entity.Category;
import java.util.stream.Collectors;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class ListCategoriesUseCase {

    private final CategoryUsageGateway usageGateway;
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public ListCategoriesUseCase(CategoryUsageGateway usageGateway,
            CategoryRepository categoryRepository,
            CategoryMapper categoryMapper) {
        this.usageGateway = usageGateway;
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<CategoryListResponse> execute(String search,
            Boolean active, int page, int size, String orderBy,
            String orderDir) {
        PageResponse<Category> summaryPage = categoryRepository
                .findAllWithFilters(search, active, page, size, orderBy,
                        orderDir);

        var counts = usageGateway
                .countActiveProductsByCategoryIds(summaryPage.content().stream()
                        .map(Category::getId).collect(Collectors.toSet()));

        List<CategoryListResponse> content = summaryPage.content().stream()
                .map(category -> categoryMapper
                        .toListResponse(new CategorySummary(category,
                                counts.getOrDefault(category.getId(), 0L))))
                .toList();

        return new PageResponse<>(content, summaryPage.totalElements(),
                summaryPage.totalPages(), summaryPage.size(),
                summaryPage.number(), summaryPage.first(), summaryPage.last(),
                summaryPage.empty());
    }
}
