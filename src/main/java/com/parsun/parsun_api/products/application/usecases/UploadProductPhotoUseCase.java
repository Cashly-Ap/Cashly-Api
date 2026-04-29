package com.parsun.parsun_api.products.application.usecases;

import com.parsun.parsun_api.products.application.dto.response.ProductDetailResponse;
import com.parsun.parsun_api.products.application.exceptions.ProductNotFoundException;
import com.parsun.parsun_api.products.application.mappers.ProductMapper;
import com.parsun.parsun_api.products.domain.entities.Product;
import com.parsun.parsun_api.products.domain.repositories.ProductRepository;
import com.parsun.parsun_api.shared.storage.domain.FileStorageService;
import com.parsun.parsun_api.categories.domain.repositories.CategoryRepository;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class UploadProductPhotoUseCase {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final FileStorageService fileStorageService;
    private final ProductMapper productMapper;

    public UploadProductPhotoUseCase(ProductRepository productRepository,
                                      CategoryRepository categoryRepository,
                                      FileStorageService fileStorageService,
                                      ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
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
                .collect(Collectors.toMap(Product::getId, p -> p));

        return productMapper.toDetailResponse(saved, categoryName, componentProducts);
    }
}
