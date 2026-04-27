package com.parsun.parsun_api.products.application.usecases;

import com.parsun.parsun_api.products.application.exceptions.ProductNotFoundException;
import com.parsun.parsun_api.products.domain.repositories.ProductRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class DeleteProductUseCase {

    private final ProductRepository productRepository;

    public DeleteProductUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public void execute(UUID id) {
        if (!productRepository.findById(id).isPresent()) {
            throw new ProductNotFoundException(id);
        }
        productRepository.delete(id);
    }
}
