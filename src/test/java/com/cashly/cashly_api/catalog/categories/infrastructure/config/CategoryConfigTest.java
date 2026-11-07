package com.cashly.cashly_api.catalog.categories.infrastructure.config;

import com.cashly.cashly_api.catalog.categories.application.mappers.CategoryMapper;
import com.cashly.cashly_api.catalog.categories.application.create.use_case.CreateCategoryUseCase;
import com.cashly.cashly_api.catalog.categories.application.delete.use_case.DeleteCategoryUseCase;
import com.cashly.cashly_api.catalog.categories.application.queries.use_case.GetCategoryUseCase;
import com.cashly.cashly_api.catalog.categories.application.queries.use_case.ListCategoriesUseCase;
import com.cashly.cashly_api.catalog.categories.application.update.use_case.UpdateCategoryUseCase;
import com.cashly.cashly_api.catalog.categories.application.ports.out.CategoryUsageGateway;
import com.cashly.cashly_api.catalog.categories.domain.repository.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryConfig Unit Tests")
class CategoryConfigTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryUsageGateway usageGateway;

    @Mock
    private CategoryMapper categoryMapper;

    @Test
    void should_CreateCreateCategoryUseCase() {
        CategoryConfig config = new CategoryConfig();

        CreateCategoryUseCase useCase = config
                .createCategoryUseCase(categoryRepository, categoryMapper, org.mockito.Mockito.mock(com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder.class));

        assertNotNull(useCase);
    }

    @Test
    void should_CreateDeleteCategoryUseCase() {
        CategoryConfig config = new CategoryConfig();

        DeleteCategoryUseCase useCase = config
                .deleteCategoryUseCase(usageGateway, categoryRepository, org.mockito.Mockito.mock(com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder.class));

        assertNotNull(useCase);
    }

    @Test
    void should_CreateGetCategoryUseCase() {
        CategoryConfig config = new CategoryConfig();

        GetCategoryUseCase useCase = config.getCategoryUseCase(usageGateway,
                categoryRepository, categoryMapper);

        assertNotNull(useCase);
    }

    @Test
    void should_CreateListCategoriesUseCase() {
        CategoryConfig config = new CategoryConfig();

        ListCategoriesUseCase useCase = config.listCategoriesUseCase(
                usageGateway, categoryRepository, categoryMapper);

        assertNotNull(useCase);
    }

    @Test
    void should_CreateUpdateCategoryUseCase() {
        CategoryConfig config = new CategoryConfig();

        UpdateCategoryUseCase useCase = config
                .updateCategoryUseCase(categoryRepository, categoryMapper, org.mockito.Mockito.mock(com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder.class));

        assertNotNull(useCase);
    }
}
