package com.parsun.parsun_api.categories.infrastructure.config;

import com.parsun.parsun_api.categories.application.mappers.CategoryMapper;
import com.parsun.parsun_api.categories.application.usecases.CreateCategoryUseCase;
import com.parsun.parsun_api.categories.application.usecases.DeleteCategoryUseCase;
import com.parsun.parsun_api.categories.application.usecases.GetCategoryUseCase;
import com.parsun.parsun_api.categories.application.usecases.ListCategoriesUseCase;
import com.parsun.parsun_api.categories.application.usecases.UpdateCategoryUseCase;
import com.parsun.parsun_api.categories.domain.repositories.CategoryRepository;
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

        DeleteCategoryUseCase useCase = config.deleteCategoryUseCase(categoryRepository);

        assertNotNull(useCase);
    }

    @Test
    void should_CreateGetCategoryUseCase() {
        CategoryConfig config = new CategoryConfig();

        GetCategoryUseCase useCase = config.getCategoryUseCase(categoryRepository, categoryMapper);

        assertNotNull(useCase);
    }

    @Test
    void should_CreateListCategoriesUseCase() {
        CategoryConfig config = new CategoryConfig();

        ListCategoriesUseCase useCase = config.listCategoriesUseCase(categoryRepository, categoryMapper);

        assertNotNull(useCase);
    }

    @Test
    void should_CreateUpdateCategoryUseCase() {
        CategoryConfig config = new CategoryConfig();

        UpdateCategoryUseCase useCase = config.updateCategoryUseCase(categoryRepository, categoryMapper);

        assertNotNull(useCase);
    }
}
