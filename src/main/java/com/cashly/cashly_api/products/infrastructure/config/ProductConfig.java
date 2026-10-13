package com.cashly.cashly_api.products.infrastructure.config;

import com.cashly.cashly_api.products.application.ports.out.ProductCategoryGateway;
import com.cashly.cashly_api.products.application.ports.out.ProductPriceTypeGateway;
import com.cashly.cashly_api.products.application.mappers.ProductMapper;
import com.cashly.cashly_api.products.application.usecases.*;
import com.cashly.cashly_api.products.domain.repositories.ProductRepository;
import com.cashly.cashly_api.products.application.ports.out.ProductFileStorageGateway;
import com.cashly.cashly_api.products.application.ports.out.ProductWarehouseStockGateway;
import com.cashly.cashly_api.products.application.ports.out.CustomerProductContextGateway;
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
                                                      ProductCategoryGateway categoryRepository,
                                                      ProductPriceTypeGateway priceTypeRepository,
                                                      ProductMapper productMapper) {
        return new CreateProductUseCase(productRepository, categoryRepository, priceTypeRepository, productMapper);
    }

    @Bean
    public GetProductUseCase getProductUseCase(ProductRepository productRepository,
                                               ProductCategoryGateway categoryRepository,
                                               ProductPriceTypeGateway priceTypeRepository,
                                               ProductMapper productMapper) {
        return new GetProductUseCase(productRepository, categoryRepository, priceTypeRepository, productMapper);
    }

    @Bean
    public ListProductsUseCase listProductsUseCase(ProductRepository productRepository,
                                                    ProductCategoryGateway categoryRepository,
                                                    ProductMapper productMapper) {
        return new ListProductsUseCase(productRepository, categoryRepository, productMapper);
    }

    @Bean
    public UpdateProductUseCase updateProductUseCase(ProductRepository productRepository,
                                                      ProductCategoryGateway categoryRepository,
                                                      ProductPriceTypeGateway priceTypeRepository,
                                                      ProductMapper productMapper) {
        return new UpdateProductUseCase(productRepository, categoryRepository, priceTypeRepository, productMapper);
    }

    @Bean
    public DeleteProductUseCase deleteProductUseCase(ProductRepository productRepository) {
        return new DeleteProductUseCase(productRepository);
    }

    @Bean
    public UploadProductPhotoUseCase uploadProductPhotoUseCase(ProductRepository productRepository,
                                                                ProductCategoryGateway categoryRepository,
                                                                ProductPriceTypeGateway priceTypeRepository,
                                                                ProductFileStorageGateway fileStorageService,
                                                                ProductMapper productMapper) {
        return new UploadProductPhotoUseCase(productRepository, categoryRepository, priceTypeRepository, fileStorageService, productMapper);
    }

    @Bean
    public RemoveProductPhotoUseCase removeProductPhotoUseCase(ProductRepository productRepository,
                                                                ProductFileStorageGateway fileStorageService) {
        return new RemoveProductPhotoUseCase(productRepository, fileStorageService);
    }

    @Bean
    public GetProductStockUseCase getProductStockUseCase(ProductRepository productRepository,
                                                          ProductWarehouseStockGateway stocks) {
        return new GetProductStockUseCase(productRepository, stocks);
    }

    @Bean
    public GetProductsForTransactionUseCase getProductsForTransactionUseCase(
            CustomerProductContextGateway customers,
            ProductRepository productRepository,
            ProductCategoryGateway categoryRepository,
            ProductWarehouseStockGateway stocks) {
        return new GetProductsForTransactionUseCase(customers, productRepository, categoryRepository, stocks);
    }
}
