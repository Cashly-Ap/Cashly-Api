package com.cashly.cashly_api.products.infrastructure.config;

import com.cashly.cashly_api.categories.domain.repositories.CategoryRepository;
import com.cashly.cashly_api.customers.domain.repositories.CustomerRepository;
import com.cashly.cashly_api.customers.domain.repositories.CustomerWarehouseAccessRepository;
import com.cashly.cashly_api.price_types.domain.repositories.PriceTypeRepository;
import com.cashly.cashly_api.products.application.mappers.ProductMapper;
import com.cashly.cashly_api.products.application.usecases.*;
import com.cashly.cashly_api.products.domain.repositories.ProductRepository;
import com.cashly.cashly_api.shared.storage.domain.FileStorageService;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseRepository;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseStockRepository;
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
                                                      PriceTypeRepository priceTypeRepository,
                                                      ProductMapper productMapper) {
        return new CreateProductUseCase(productRepository, categoryRepository, priceTypeRepository, productMapper);
    }

    @Bean
    public GetProductUseCase getProductUseCase(ProductRepository productRepository,
                                               CategoryRepository categoryRepository,
                                               PriceTypeRepository priceTypeRepository,
                                               ProductMapper productMapper) {
        return new GetProductUseCase(productRepository, categoryRepository, priceTypeRepository, productMapper);
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
                                                      PriceTypeRepository priceTypeRepository,
                                                      ProductMapper productMapper) {
        return new UpdateProductUseCase(productRepository, categoryRepository, priceTypeRepository, productMapper);
    }

    @Bean
    public DeleteProductUseCase deleteProductUseCase(ProductRepository productRepository) {
        return new DeleteProductUseCase(productRepository);
    }

    @Bean
    public UploadProductPhotoUseCase uploadProductPhotoUseCase(ProductRepository productRepository,
                                                                CategoryRepository categoryRepository,
                                                                PriceTypeRepository priceTypeRepository,
                                                                FileStorageService fileStorageService,
                                                                ProductMapper productMapper) {
        return new UploadProductPhotoUseCase(productRepository, categoryRepository, priceTypeRepository, fileStorageService, productMapper);
    }

    @Bean
    public RemoveProductPhotoUseCase removeProductPhotoUseCase(ProductRepository productRepository,
                                                                FileStorageService fileStorageService) {
        return new RemoveProductPhotoUseCase(productRepository, fileStorageService);
    }

    @Bean
    public GetProductStockUseCase getProductStockUseCase(ProductRepository productRepository,
                                                          WarehouseStockRepository warehouseStockRepository,
                                                          WarehouseRepository warehouseRepository) {
        return new GetProductStockUseCase(productRepository, warehouseStockRepository, warehouseRepository);
    }

    @Bean
    public GetProductsForTransactionUseCase getProductsForTransactionUseCase(
            CustomerRepository customerRepository,
            CustomerWarehouseAccessRepository customerWarehouseAccessRepository,
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            WarehouseStockRepository warehouseStockRepository,
            WarehouseRepository warehouseRepository) {
        return new GetProductsForTransactionUseCase(customerRepository, customerWarehouseAccessRepository,
                productRepository, categoryRepository, warehouseStockRepository, warehouseRepository);
    }
}
