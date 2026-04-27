package com.parsun.parsun_api.products.infrastructure.config;

import com.parsun.parsun_api.categories.domain.repositories.CategoryRepository;
import com.parsun.parsun_api.products.application.mappers.ProductMapper;
import com.parsun.parsun_api.products.application.usecases.*;
import com.parsun.parsun_api.products.domain.repositories.ProductRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProductConfig {

    @Bean
    public ProductMapper productMapper() {
        return new ProductMapper();
    }

    @Bean
    public CreateProductUseCase createProductUseCase(ProductRepository productRepository,
                                                      CategoryRepository categoryRepository,
                                                      ProductMapper productMapper) {
        return new CreateProductUseCase(productRepository, categoryRepository, productMapper);
    }

    @Bean
    public GetProductUseCase getProductUseCase(ProductRepository productRepository,
                                                CategoryRepository categoryRepository,
                                                ProductMapper productMapper) {
        return new GetProductUseCase(productRepository, categoryRepository, productMapper);
    }

    @Bean
    public ListProductsUseCase listProductsUseCase(ProductRepository productRepository,
                                                    CategoryRepository categoryRepository,
                                                    ProductMapper productMapper) {
        return new ListProductsUseCase(productRepository, categoryRepository, productMapper);
    }

    @Bean
    public UpdateProductUseCase updateProductUseCase(ProductRepository productRepository,
                                                      CategoryRepository categoryRepository,
                                                      ProductMapper productMapper) {
        return new UpdateProductUseCase(productRepository, categoryRepository, productMapper);
    }

    @Bean
    public DeleteProductUseCase deleteProductUseCase(ProductRepository productRepository) {
        return new DeleteProductUseCase(productRepository);
    }
}
