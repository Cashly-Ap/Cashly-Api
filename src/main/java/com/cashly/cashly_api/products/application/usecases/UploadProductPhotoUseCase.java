package com.cashly.cashly_api.products.application.usecases;

import com.cashly.cashly_api.products.application.dto.response.ProductDetailResponse;
import com.cashly.cashly_api.products.application.exceptions.ProductNotFoundException;
import com.cashly.cashly_api.products.application.mappers.ProductMapper;
import com.cashly.cashly_api.products.domain.entities.Product;
import com.cashly.cashly_api.products.domain.repositories.ProductRepository;
import com.cashly.cashly_api.products.domain.valueobjects.ProductPrice;
import com.cashly.cashly_api.products.application.ports.out.ProductCategoryGateway;
import com.cashly.cashly_api.products.application.ports.out.ProductFileStorageGateway;
import com.cashly.cashly_api.products.application.ports.out.ProductPriceTypeGateway;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class UploadProductPhotoUseCase {

    private final ProductRepository productRepository;
    private final ProductCategoryGateway categoryRepository;
    private final ProductPriceTypeGateway priceTypeRepository;
    private final ProductFileStorageGateway fileStorageService;
    private final ProductMapper productMapper;

    public UploadProductPhotoUseCase(ProductRepository productRepository,
                                      ProductCategoryGateway categoryRepository,
                                      ProductPriceTypeGateway priceTypeRepository,
                                      ProductFileStorageGateway fileStorageService,
                                      ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.priceTypeRepository = priceTypeRepository;
        this.fileStorageService = fileStorageService;
        this.productMapper = productMapper;
    }

    @Transactional
    public ProductDetailResponse execute(UUID productId, String originalFilename,
                                          InputStream inputStream, String contentType) {
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
                .map(c -> c.name())
                .orElse("");

        Map<UUID, Product> componentProducts = saved.getComponents().stream()
                .map(c -> productRepository.findById(c.getComponentProductId()).orElse(null))
                .filter(p -> p != null)
                .collect(Collectors.toMap(Product::getId, p -> p, (a, b) -> a));

        Map<UUID, String> priceTypeNames = saved.getProductPrices().stream()
                .collect(Collectors.toMap(
                    ProductPrice::getPriceTypeId,
                    pp -> priceTypeRepository.findById(pp.getPriceTypeId())
                        .map(pt -> pt.name())
                        .orElse("")
                ));

        return productMapper.toDetailResponse(saved, categoryName, componentProducts, priceTypeNames);
    }
}
