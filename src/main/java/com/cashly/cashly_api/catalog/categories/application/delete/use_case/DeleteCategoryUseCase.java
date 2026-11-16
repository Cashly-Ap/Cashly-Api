package com.cashly.cashly_api.catalog.categories.application.delete.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.catalog.categories.application.exceptions.CategoryHasActiveProductsException;
import com.cashly.cashly_api.catalog.categories.application.exceptions.CategoryNotFoundException;
import com.cashly.cashly_api.catalog.categories.domain.entity.Category;
import com.cashly.cashly_api.catalog.categories.application.ports.out.CategoryUsageGateway;
import com.cashly.cashly_api.catalog.categories.domain.repository.CategoryRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import com.cashly.cashly_api.platform.audit.application.ports.in.Audited;

public class DeleteCategoryUseCase {

    private final CategoryUsageGateway usageGateway;
    private final CategoryRepository categoryRepository;

    private final ActivityNotificationRecorder activityNotifications;

    public DeleteCategoryUseCase(CategoryUsageGateway usageGateway,
            CategoryRepository categoryRepository,
            ActivityNotificationRecorder activityNotifications) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.usageGateway = usageGateway;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    @Audited(module = "catalog", entityType = "CATEGORY", action = "DELETE")
    public void execute(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        long activeProductCount = usageGateway
                .countActiveProductsByCategory(id);
        if (activeProductCount > 0) {
            throw new CategoryHasActiveProductsException(id);
        }

        category.deactivate();
        activityNotifications.record(ActivityEvent.of("CATALOG_CATEGORIES",
                "DELETE", "CATEGORY", category.getId(),
                category.getName(), null));
        categoryRepository.save(category);
    }
}
