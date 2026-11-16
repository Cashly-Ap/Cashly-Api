package com.cashly.cashly_api.catalog.products.application.photos.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.catalog.products.application.dto.response.ProductDetailResponse;
import com.cashly.cashly_api.catalog.products.application.exceptions.ProductNotFoundException;
import com.cashly.cashly_api.catalog.products.application.mappers.ProductMapper;
import com.cashly.cashly_api.catalog.products.domain.entity.Product;
import com.cashly.cashly_api.catalog.products.domain.repository.ProductRepository;
import com.cashly.cashly_api.catalog.products.domain.valueobjects.ProductPrice;
import com.cashly.cashly_api.catalog.products.application.ports.out.ProductCategoryGateway;
import com.cashly.cashly_api.catalog.products.application.ports.out.ProductFileStorageGateway;
import com.cashly.cashly_api.catalog.products.application.ports.out.ProductPriceTypeGateway;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import com.cashly.cashly_api.platform.audit.application.ports.in.Audited;

public class UploadProductPhotoUseCase {

    private final ProductRepository productRepository;
    private final ProductCategoryGateway categoryRepository;
    private final ProductPriceTypeGateway priceTypeRepository;
    private final ProductFileStorageGateway fileStorageService;
    private final ProductMapper productMapper;

    private final ActivityNotificationRecorder activityNotifications;

    public UploadProductPhotoUseCase(ProductRepository productRepository,
            ProductCategoryGateway categoryRepository,
            ProductPriceTypeGateway priceTypeRepository,
            ProductFileStorageGateway fileStorageService,
            ProductMapper productMapper,
            ActivityNotificationRecorder activityNotifications) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.priceTypeRepository = priceTypeRepository;
        this.fileStorageService = fileStorageService;
        this.productMapper = productMapper;
    }

    @Transactional
    @Audited(module = "catalog", entityType = "PRODUCT", action = "UPLOAD_PHOTO")
    public ProductDetailResponse execute(UUID productId,
            String originalFilename, InputStream inputStream,
            String contentType) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        if (product.getPhoto() != null) {
            fileStorageService.remove(product.getPhoto());
        }

        String storedPath = fileStorageService.store(
                "products/" + originalFilename, inputStream, contentType);

        product.assignPhoto(storedPath);
        Product saved = productRepository.save(product);

        String categoryName = categoryRepository.findById(saved.getCategoryId())
                .map(c -> c.name()).orElse("");

        Map<UUID, Product> componentProducts = saved.getComponents().stream()
                .map(c -> productRepository.findById(c.getComponentProductId())
                        .orElse(null))
                .filter(p -> p != null)
                .collect(Collectors.toMap(Product::getId, p -> p, (a, b) -> a));

        Map<UUID, String> priceTypeNames = saved.getProductPrices().stream()
                .collect(Collectors.toMap(ProductPrice::getPriceTypeId,
                        pp -> priceTypeRepository.findById(pp.getPriceTypeId())
                                .map(pt -> pt.name()).orElse("")));

        activityNotifications.record(ActivityEvent.of("CATALOG_PRODUCTS",
                "EDIT", "PRODUCT", saved.getId(),
                saved.getCode(), null));
        return productMapper.toDetailResponse(saved, categoryName,
                componentProducts, priceTypeNames);
    }
}
