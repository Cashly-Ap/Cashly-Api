package com.cashly.cashly_api.catalog.products.application.update.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.catalog.products.application.ports.out.ProductCategoryGateway;
import com.cashly.cashly_api.catalog.products.application.ports.out.ProductPriceTypeGateway;
import com.cashly.cashly_api.catalog.products.application.dto.request.ProductPriceRequest;
import com.cashly.cashly_api.catalog.products.application.update.request.UpdateProductRequest;
import com.cashly.cashly_api.catalog.products.application.dto.response.ProductDetailResponse;
import com.cashly.cashly_api.catalog.products.application.exceptions.CompositeProductCannotBeComponentException;
import com.cashly.cashly_api.catalog.products.application.exceptions.DuplicateProductComponentException;
import com.cashly.cashly_api.catalog.products.application.exceptions.ProductNotFoundException;
import com.cashly.cashly_api.catalog.products.application.mappers.ProductMapper;
import com.cashly.cashly_api.catalog.products.domain.entity.Product;
import com.cashly.cashly_api.catalog.products.domain.entity.ProductComponent;
import com.cashly.cashly_api.catalog.products.domain.repository.ProductRepository;
import com.cashly.cashly_api.catalog.products.domain.valueobjects.ProductPrice;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import com.cashly.cashly_api.platform.audit.application.ports.in.Audited;

public class UpdateProductUseCase {

    private final ProductRepository productRepository;
    private final ProductCategoryGateway categoryRepository;
    private final ProductPriceTypeGateway priceTypeRepository;
    private final ProductMapper productMapper;

    private final ActivityNotificationRecorder activityNotifications;

    public UpdateProductUseCase(ProductRepository productRepository,
            ProductCategoryGateway categoryRepository,
            ProductPriceTypeGateway priceTypeRepository,
            ProductMapper productMapper,
            ActivityNotificationRecorder activityNotifications) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.priceTypeRepository = priceTypeRepository;
        this.productMapper = productMapper;
    }

    @Transactional
    @Audited(module = "catalog", entityType = "PRODUCT", action = "EDIT")
    public ProductDetailResponse execute(UUID id,
            UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        var category = categoryRepository.require(request.categoryId());

        product.update(request.name(), request.description(),
                request.categoryId(), request.purchasePrice(),
                request.active());

        if (Boolean.TRUE.equals(request.composite())
                && !product.isComposite()) {
            product.makeComposite();
        }

        if (product.isComposite() && request.components() != null) {
            Set<UUID> seen = new HashSet<>();
            List<ProductComponent> components = request.components().stream()
                    .map(r -> {
                        if (!seen.add(r.componentProductId())) {
                            throw new DuplicateProductComponentException(
                                    r.componentProductId());
                        }
                        Product comp = productRepository
                                .findById(r.componentProductId())
                                .orElseThrow(() -> new ProductNotFoundException(
                                        r.componentProductId()));
                        if (comp.isComposite()) {
                            throw new CompositeProductCannotBeComponentException(
                                    r.componentProductId());
                        }
                        return new ProductComponent(UUID.randomUUID(), id,
                                r.componentProductId(), r.quantity());
                    }).toList();
            product.setComponents(components);
        }

        if (request.productPrices() != null) {
            List<ProductPrice> productPrices = buildProductPrices(
                    request.productPrices());
            product.setProductPrices(productPrices);
        }

        Product saved = productRepository.save(product);

        Map<UUID, Product> componentProducts = saved.getComponents().stream()
                .map(c -> productRepository.findById(c.getComponentProductId())
                        .orElse(null))
                .filter(p -> p != null)
                .collect(Collectors.toMap(Product::getId, p -> p, (a, b) -> a));

        Map<UUID, String> priceTypeNames = resolvePriceTypeNames(
                saved.getProductPrices());
        activityNotifications.record(ActivityEvent.of("CATALOG_PRODUCTS",
                "EDIT", "PRODUCT", saved.getId(),
                saved.getCode(), null));
        return productMapper.toDetailResponse(saved, category.name(),
                componentProducts, priceTypeNames);
    }

    private List<ProductPrice> buildProductPrices(
            List<ProductPriceRequest> requests) {
        if (requests.isEmpty()) {
            return List.of();
        }
        return requests.stream().map(r -> {
            priceTypeRepository.require(r.priceTypeId());
            return ProductPrice.create(r.priceTypeId(), r.price(),
                    r.profitMargin());
        }).toList();
    }

    private Map<UUID, String> resolvePriceTypeNames(List<ProductPrice> prices) {
        return prices.stream()
                .collect(Collectors.toMap(ProductPrice::getPriceTypeId,
                        pp -> priceTypeRepository.findById(pp.getPriceTypeId())
                                .map(pt -> pt.name()).orElse("")));
    }
}
