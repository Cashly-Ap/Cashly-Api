package com.cashly.cashly_api.categories.infrastructure.config;

import com.cashly.cashly_api.categories.application.mappers.CategoryMapper;
import com.cashly.cashly_api.categories.application.create.CreateCategoryUseCase;
import com.cashly.cashly_api.categories.application.delete.DeleteCategoryUseCase;
import com.cashly.cashly_api.categories.application.queries.GetCategoryUseCase;
import com.cashly.cashly_api.categories.application.queries.ListCategoriesUseCase;
import com.cashly.cashly_api.categories.application.update.UpdateCategoryUseCase;
import com.cashly.cashly_api.categories.application.ports.out.CategoryUsageGateway;
import com.cashly.cashly_api.categories.domain.CategoryRepository;
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

    @Mock private CategoryUsageGateway usageGateway;

    @Mock
    private CategoryMapper categoryMapper;

    @Test
    void should_CreateCreateCategoryUseCase() {
        CategoryConfig config = new CategoryConfig();

        CreateCategoryUseCase useCase = config.createCategoryUseCase(categoryRepository, categoryMapper);

        assertNotNull(useCase);
    }

    @Test
    void should_CreateDeleteCategoryUseCase() {
        CategoryConfig config = new CategoryConfig();

        DeleteCategoryUseCase useCase = config.deleteCategoryUseCase(usageGateway, categoryRepository);

        assertNotNull(useCase);
    }

    @Test
    void should_CreateGetCategoryUseCase() {
        CategoryConfig config = new CategoryConfig();

        GetCategoryUseCase useCase = config.getCategoryUseCase(usageGateway, categoryRepository, categoryMapper);

        assertNotNull(useCase);
    }

    @Test
    void should_CreateListCategoriesUseCase() {
        CategoryConfig config = new CategoryConfig();

        ListCategoriesUseCase useCase = config.listCategoriesUseCase(usageGateway, categoryRepository, categoryMapper);

        assertNotNull(useCase);
    }

    @Test
    void should_CreateUpdateCategoryUseCase() {
        CategoryConfig config = new CategoryConfig();

        UpdateCategoryUseCase useCase = config.updateCategoryUseCase(categoryRepository, categoryMapper);

        assertNotNull(useCase);
    }
}
