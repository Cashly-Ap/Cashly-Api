package com.cashly.cashly_api.catalog.products.application.create.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.catalog.products.application.ports.out.ProductCategoryGateway;
import com.cashly.cashly_api.catalog.products.application.ports.out.ProductPriceTypeGateway;
import com.cashly.cashly_api.catalog.products.application.create.request.CreateProductRequest;
import com.cashly.cashly_api.catalog.products.application.dto.request.ProductPriceRequest;
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

public class CreateProductUseCase {

    private final ProductRepository productRepository;
    private final ProductCategoryGateway categoryRepository;
    private final ProductPriceTypeGateway priceTypeRepository;
    private final ProductMapper productMapper;

    private final ActivityNotificationRecorder activityNotifications;

    public CreateProductUseCase(ProductRepository productRepository,
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
    public ProductDetailResponse execute(CreateProductRequest request) {
        var category = categoryRepository.require(request.categoryId());

        String code = request.code() != null && !request.code().isBlank()
                ? request.code()
                : productRepository.generateNextCode();

        List<ProductPrice> productPrices = buildProductPrices(
                request.productPrices());

        Product product = Product.create(code, request.name(),
                request.description(), request.categoryId(),
                Boolean.TRUE.equals(request.composite()),
                request.purchasePrice());

        product.setProductPrices(productPrices);

        Product saved = productRepository.save(product);

        if (Boolean.TRUE.equals(request.composite())
                && request.components() != null
                && !request.components().isEmpty()) {
            List<ProductComponent> components = buildComponents(saved.getId(),
                    request.components());
            saved.setComponents(components);
            saved = productRepository.save(saved);
        }

        Map<UUID, Product> componentProducts = loadComponentProducts(saved);
        Map<UUID, String> priceTypeNames = resolvePriceTypeNames(
                saved.getProductPrices());
        activityNotifications.record(ActivityEvent.of("CATALOG_PRODUCTS",
                "CREATE", "PRODUCT", saved.getId(),
                saved.getCode(), null));
        return productMapper.toDetailResponse(saved, category.name(),
                componentProducts, priceTypeNames);
    }

    private List<ProductPrice> buildProductPrices(
            List<ProductPriceRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }
        return requests.stream().map(r -> {
            priceTypeRepository.require(r.priceTypeId());
            return ProductPrice.create(r.priceTypeId(), r.price(),
                    r.profitMargin());
        }).toList();
    }

    private List<ProductComponent> buildComponents(UUID parentId,
            List<CreateProductRequest.ComponentRequest> requests) {
        Set<UUID> seen = new HashSet<>();
        return requests.stream().map(r -> {
            if (!seen.add(r.componentProductId())) {
                throw new DuplicateProductComponentException(
                        r.componentProductId());
            }
            Product comp = productRepository.findById(r.componentProductId())
                    .orElseThrow(() -> new ProductNotFoundException(
                            r.componentProductId()));
            if (comp.isComposite()) {
                throw new CompositeProductCannotBeComponentException(
                        r.componentProductId());
            }
            return new ProductComponent(UUID.randomUUID(), parentId,
                    r.componentProductId(), r.quantity());
        }).toList();
    }

    private Map<UUID, Product> loadComponentProducts(Product product) {
        return product.getComponents().stream()
                .map(c -> productRepository.findById(c.getComponentProductId())
                        .orElse(null))
                .filter(p -> p != null)
                .collect(Collectors.toMap(Product::getId, p -> p));
    }

    private Map<UUID, String> resolvePriceTypeNames(List<ProductPrice> prices) {
        return prices.stream()
                .collect(Collectors.toMap(ProductPrice::getPriceTypeId,
                        pp -> priceTypeRepository.findById(pp.getPriceTypeId())
                                .map(pt -> pt.name()).orElse("")));
    }
}
