package com.parsun.parsun_api.products.application.usecases;

import com.parsun.parsun_api.products.domain.entities.Product;
import com.parsun.parsun_api.products.application.exceptions.ProductNotFoundException;
import com.parsun.parsun_api.products.domain.repositories.ProductRepository;
import com.parsun.parsun_api.shared.storage.domain.FileStorageService;

import java.util.UUID;

public class RemoveProductPhotoUseCase {

    private final ProductRepository productRepository;
    private final FileStorageService fileStorageService;

    public RemoveProductPhotoUseCase(ProductRepository productRepository,
                                      FileStorageService fileStorageService) {
        this.productRepository = productRepository;
        this.fileStorageService = fileStorageService;
    }

    public void execute(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        if (product.getPhoto() != null) {
            fileStorageService.delete(product.getPhoto());
        }

        product.removePhoto();
        productRepository.save(product);
    }
}
