package com.cashly.cashly_api.catalog.products.application.photos.use_case;

import com.cashly.cashly_api.catalog.products.domain.entity.Product;
import com.cashly.cashly_api.catalog.products.application.exceptions.ProductNotFoundException;
import com.cashly.cashly_api.catalog.products.domain.repository.ProductRepository;
import com.cashly.cashly_api.catalog.products.application.ports.out.ProductFileStorageGateway;

import java.util.UUID;

public class RemoveProductPhotoUseCase {

    private final ProductRepository productRepository;
    private final ProductFileStorageGateway fileStorageService;

    public RemoveProductPhotoUseCase(ProductRepository productRepository,
            ProductFileStorageGateway fileStorageService) {
        this.productRepository = productRepository;
        this.fileStorageService = fileStorageService;
    }

    public void execute(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        if (product.getPhoto() != null) {
            fileStorageService.remove(product.getPhoto());
        }

        product.removePhoto();
        productRepository.save(product);
    }
}
