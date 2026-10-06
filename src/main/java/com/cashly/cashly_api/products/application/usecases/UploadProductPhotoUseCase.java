package com.cashly.cashly_api.products.application.usecases;

import com.cashly.cashly_api.products.application.dto.response.ProductDetailResponse;
import com.cashly.cashly_api.products.application.exceptions.ProductNotFoundException;
import com.cashly.cashly_api.products.application.mappers.ProductMapper;
import com.cashly.cashly_api.products.domain.entities.Product;
import com.cashly.cashly_api.products.domain.repositories.ProductRepository;
import com.cashly.cashly_api.products.domain.valueobjects.ProductPrice;
import com.cashly.cashly_api.shared.storage.domain.FileStorageService;
import com.cashly.cashly_api.categories.domain.repositories.CategoryRepository;
import com.cashly.cashly_api.price_types.domain.repositories.PriceTypeRepository;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class UploadProductPhotoUseCase {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final PriceTypeRepository priceTypeRepository;
    private final FileStorageService fileStorageService;
    private final ProductMapper productMapper;

    public UploadProductPhotoUseCase(ProductRepository productRepository,
                                      CategoryRepository categoryRepository,
                                      PriceTypeRepository priceTypeRepository,
                                      FileStorageService fileStorageService,
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
            fileStorageService.delete(product.getPhoto());
        }

        String storedPath = fileStorageService.store(
                "products/" + originalFilename, inputStream, contentType);

        product.assignPhoto(storedPath);
        Product saved = productRepository.save(product);

        String categoryName = categoryRepository.findById(saved.getCategoryId())
                .map(c -> c.getName())
                .orElse("");

        Map<UUID, Product> componentProducts = saved.getComponents().stream()
                .map(c -> productRepository.findById(c.getComponentProductId()).orElse(null))
                .filter(p -> p != null)
                .collect(Collectors.toMap(Product::getId, p -> p, (a, b) -> a));

        Map<UUID, String> priceTypeNames = saved.getProductPrices().stream()
                .collect(Collectors.toMap(
                    ProductPrice::getPriceTypeId,
                    pp -> priceTypeRepository.findById(pp.getPriceTypeId())
                        .map(pt -> pt.getName())
                        .orElse("")
                ));

        return productMapper.toDetailResponse(saved, categoryName, componentProducts, priceTypeNames);
    }
}
