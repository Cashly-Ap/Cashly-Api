package com.cashly.cashly_api.catalog.categories.application.create.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.catalog.categories.application.create.request.CreateCategoryRequest;
import com.cashly.cashly_api.catalog.categories.application.dto.response.CategoryDetailResponse;
import com.cashly.cashly_api.catalog.categories.application.exceptions.DuplicateCategoryNameException;
import com.cashly.cashly_api.catalog.categories.application.mappers.CategoryMapper;
import com.cashly.cashly_api.catalog.categories.domain.entity.Category;
import com.cashly.cashly_api.catalog.categories.domain.repository.CategoryRepository;
import org.springframework.transaction.annotation.Transactional;
import com.cashly.cashly_api.platform.audit.application.ports.in.Audited;

public class CreateCategoryUseCase {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    private final ActivityNotificationRecorder activityNotifications;

    public CreateCategoryUseCase(CategoryRepository categoryRepository,
            CategoryMapper categoryMapper,
            ActivityNotificationRecorder activityNotifications) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Transactional
    @Audited(module = "catalog", entityType = "CATEGORY", action = "CREATE")
    public CategoryDetailResponse execute(CreateCategoryRequest request) {
        if (categoryRepository.existsByName(request.name().trim())) {
            throw new DuplicateCategoryNameException(request.name());
        }
        Category category = Category.create(request.name().trim(),
                request.description());
        Category saved = categoryRepository.save(category);
        activityNotifications.record(ActivityEvent.of("CATALOG_CATEGORIES",
                "CREATE", "CATEGORY", saved.getId(),
                saved.getName(), null));
        return categoryMapper.toDetailResponse(saved, 0L);
    }
}
