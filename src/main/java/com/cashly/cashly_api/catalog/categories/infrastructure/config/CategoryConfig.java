package com.cashly.cashly_api.catalog.categories.infrastructure.config;

import com.cashly.cashly_api.catalog.categories.application.mappers.CategoryMapper;
import com.cashly.cashly_api.catalog.categories.application.create.use_case.CreateCategoryUseCase;
import com.cashly.cashly_api.catalog.categories.application.delete.use_case.DeleteCategoryUseCase;
import com.cashly.cashly_api.catalog.categories.application.queries.use_case.GetCategoryUseCase;
import com.cashly.cashly_api.catalog.categories.application.queries.use_case.ListCategoriesUseCase;
import com.cashly.cashly_api.catalog.categories.application.update.use_case.UpdateCategoryUseCase;
import com.cashly.cashly_api.catalog.categories.domain.repository.CategoryRepository;
import com.cashly.cashly_api.catalog.categories.application.ports.out.CategoryUsageGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CategoryConfig {

    @Bean
    public CategoryMapper categoryMapper() {
        return new CategoryMapper();
    }

    @Bean
    public CreateCategoryUseCase createCategoryUseCase(
            CategoryRepository categoryRepository,
            CategoryMapper categoryMapper) {
        return new CreateCategoryUseCase(categoryRepository, categoryMapper);
    }

    @Bean
    public GetCategoryUseCase getCategoryUseCase(
            CategoryUsageGateway usageGateway,
            CategoryRepository categoryRepository,
            CategoryMapper categoryMapper) {
        return new GetCategoryUseCase(usageGateway, categoryRepository,
                categoryMapper);
    }

    @Bean
    public ListCategoriesUseCase listCategoriesUseCase(
            CategoryUsageGateway usageGateway,
            CategoryRepository categoryRepository,
            CategoryMapper categoryMapper) {
        return new ListCategoriesUseCase(usageGateway, categoryRepository,
                categoryMapper);
    }

    @Bean
    public UpdateCategoryUseCase updateCategoryUseCase(
            CategoryRepository categoryRepository,
            CategoryMapper categoryMapper) {
        return new UpdateCategoryUseCase(categoryRepository, categoryMapper);
    }

    @Bean
    public DeleteCategoryUseCase deleteCategoryUseCase(
            CategoryUsageGateway usageGateway,
            CategoryRepository categoryRepository) {
        return new DeleteCategoryUseCase(usageGateway, categoryRepository);
    }
}
