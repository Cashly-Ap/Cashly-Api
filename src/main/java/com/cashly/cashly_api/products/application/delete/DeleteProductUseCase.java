package com.cashly.cashly_api.products.application.delete;

import com.cashly.cashly_api.products.application.exceptions.ProductHasAssociatedRecordsException;
import com.cashly.cashly_api.products.application.exceptions.ProductNotFoundException;
import com.cashly.cashly_api.products.domain.ProductRepository;
import com.cashly.cashly_api.products.domain.exceptions.ProductReferencedException;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class DeleteProductUseCase {

    private final ProductRepository productRepository;

    public DeleteProductUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public void execute(UUID id) {
        if (productRepository.findById(id).isEmpty()) {
            throw new ProductNotFoundException(id);
        }

        productRepository.deleteComponentReferencesByProductId(id);

        try {
            productRepository.delete(id);
        } catch (ProductReferencedException e) {
            throw new ProductHasAssociatedRecordsException(id);
        }
    }
}
