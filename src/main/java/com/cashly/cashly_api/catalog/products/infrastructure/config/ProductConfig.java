package com.cashly.cashly_api.catalog.products.infrastructure.config;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;
import com.cashly.cashly_api.catalog.products.application.ports.out.ProductCategoryGateway;
import com.cashly.cashly_api.catalog.products.application.ports.out.ProductPriceTypeGateway;
import com.cashly.cashly_api.catalog.products.application.mappers.ProductMapper;
import com.cashly.cashly_api.catalog.products.application.create.use_case.CreateProductUseCase;
import com.cashly.cashly_api.catalog.products.application.delete.use_case.DeleteProductUseCase;
import com.cashly.cashly_api.catalog.products.application.stock.use_case.GetProductStockUseCase;
import com.cashly.cashly_api.catalog.products.application.queries.use_case.GetProductUseCase;
import com.cashly.cashly_api.catalog.products.application.transactions.use_case.GetProductsForTransactionUseCase;
import com.cashly.cashly_api.catalog.products.application.queries.use_case.ListProductsUseCase;
import com.cashly.cashly_api.catalog.products.application.photos.use_case.RemoveProductPhotoUseCase;
import com.cashly.cashly_api.catalog.products.application.update.use_case.UpdateProductUseCase;
import com.cashly.cashly_api.catalog.products.application.photos.use_case.UploadProductPhotoUseCase;
import com.cashly.cashly_api.catalog.products.domain.repository.ProductRepository;
import com.cashly.cashly_api.catalog.products.application.ports.out.ProductFileStorageGateway;
import com.cashly.cashly_api.catalog.products.application.ports.out.ProductWarehouseStockGateway;
import com.cashly.cashly_api.catalog.products.application.ports.out.CustomerProductContextGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProductConfig {

    @Bean
    public ProductMapper productMapper() {
        return new ProductMapper();
    }

    @Bean
    public CreateProductUseCase createProductUseCase(
            ProductRepository productRepository,
            ProductCategoryGateway categoryRepository,
            ProductPriceTypeGateway priceTypeRepository,
            ProductMapper productMapper,
            ActivityNotificationRecorder activityNotifications) {
        return new CreateProductUseCase(productRepository, categoryRepository,
                priceTypeRepository, productMapper, activityNotifications);
    }

    @Bean
    public GetProductUseCase getProductUseCase(
            ProductRepository productRepository,
            ProductCategoryGateway categoryRepository,
            ProductPriceTypeGateway priceTypeRepository,
            ProductMapper productMapper) {
        return new GetProductUseCase(productRepository, categoryRepository,
                priceTypeRepository, productMapper);
    }

    @Bean
    public ListProductsUseCase listProductsUseCase(
            ProductRepository productRepository,
            ProductCategoryGateway categoryRepository,
            ProductMapper productMapper) {
        return new ListProductsUseCase(productRepository, categoryRepository,
                productMapper);
    }

    @Bean
    public UpdateProductUseCase updateProductUseCase(
            ProductRepository productRepository,
            ProductCategoryGateway categoryRepository,
            ProductPriceTypeGateway priceTypeRepository,
            ProductMapper productMapper,
            ActivityNotificationRecorder activityNotifications) {
        return new UpdateProductUseCase(productRepository, categoryRepository,
                priceTypeRepository, productMapper, activityNotifications);
    }

    @Bean
    public DeleteProductUseCase deleteProductUseCase(
            ProductRepository productRepository,
            ActivityNotificationRecorder activityNotifications) {
        return new DeleteProductUseCase(productRepository, activityNotifications);
    }

    @Bean
    public UploadProductPhotoUseCase uploadProductPhotoUseCase(
            ProductRepository productRepository,
            ProductCategoryGateway categoryRepository,
            ProductPriceTypeGateway priceTypeRepository,
            ProductFileStorageGateway fileStorageService,
            ProductMapper productMapper,
            ActivityNotificationRecorder activityNotifications) {
        return new UploadProductPhotoUseCase(productRepository,
                categoryRepository, priceTypeRepository, fileStorageService,
                productMapper, activityNotifications);
    }

    @Bean
    public RemoveProductPhotoUseCase removeProductPhotoUseCase(
            ProductRepository productRepository,
            ProductFileStorageGateway fileStorageService,
            ActivityNotificationRecorder activityNotifications) {
        return new RemoveProductPhotoUseCase(productRepository,
                fileStorageService, activityNotifications);
    }

    @Bean
    public GetProductStockUseCase getProductStockUseCase(
            ProductRepository productRepository,
            ProductWarehouseStockGateway stocks) {
        return new GetProductStockUseCase(productRepository, stocks);
    }

    @Bean
    public GetProductsForTransactionUseCase getProductsForTransactionUseCase(
            CustomerProductContextGateway customers,
            ProductRepository productRepository,
            ProductCategoryGateway categoryRepository,
            ProductWarehouseStockGateway stocks) {
        return new GetProductsForTransactionUseCase(customers,
                productRepository, categoryRepository, stocks);
    }
}
