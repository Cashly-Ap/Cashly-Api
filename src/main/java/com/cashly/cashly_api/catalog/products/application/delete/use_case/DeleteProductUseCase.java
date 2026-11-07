package com.cashly.cashly_api.catalog.products.application.delete.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.catalog.products.application.exceptions.ProductHasAssociatedRecordsException;
import com.cashly.cashly_api.catalog.products.application.exceptions.ProductNotFoundException;
import com.cashly.cashly_api.catalog.products.domain.repository.ProductRepository;
import com.cashly.cashly_api.catalog.products.domain.exceptions.ProductReferencedException;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class DeleteProductUseCase {

    private final ProductRepository productRepository;

    private final ActivityNotificationRecorder activityNotifications;

    public DeleteProductUseCase(ProductRepository productRepository,
            ActivityNotificationRecorder activityNotifications) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.productRepository = productRepository;
    }

    @Transactional
    public void execute(UUID id) {
        var product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        productRepository.deleteComponentReferencesByProductId(id);

        try {
            productRepository.delete(id);
        } catch (ProductReferencedException e) {
            throw new ProductHasAssociatedRecordsException(id);
        }
        activityNotifications.record(ActivityEvent.of("CATALOG_PRODUCTS",
                "DELETE", "PRODUCT", product.getId(),
                product.getCode(), null));
    }
}
