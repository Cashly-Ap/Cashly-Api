package com.cashly.cashly_api.products.application.usecases;

import com.cashly.cashly_api.products.application.contracts.ProductCategoryGateway;
import com.cashly.cashly_api.products.application.contracts.ProductPriceTypeGateway;
import com.cashly.cashly_api.products.application.dto.request.ProductPriceRequest;
import com.cashly.cashly_api.products.application.dto.request.UpdateProductRequest;
import com.cashly.cashly_api.products.application.dto.response.ProductDetailResponse;
import com.cashly.cashly_api.products.application.exceptions.CompositeProductCannotBeComponentException;
import com.cashly.cashly_api.products.application.exceptions.DuplicateProductComponentException;
import com.cashly.cashly_api.products.application.exceptions.ProductNotFoundException;
import com.cashly.cashly_api.products.application.mappers.ProductMapper;
import com.cashly.cashly_api.products.domain.entities.Product;
import com.cashly.cashly_api.products.domain.entities.ProductComponent;
import com.cashly.cashly_api.products.domain.repositories.ProductRepository;
import com.cashly.cashly_api.products.domain.valueobjects.ProductPrice;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class UpdateProductUseCase {

    private final ProductRepository productRepository;
    private final ProductCategoryGateway categoryRepository;
    private final ProductPriceTypeGateway priceTypeRepository;
    private final ProductMapper productMapper;

    public UpdateProductUseCase(ProductRepository productRepository,
                                 ProductCategoryGateway categoryRepository,
                                 ProductPriceTypeGateway priceTypeRepository,
                                 ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.priceTypeRepository = priceTypeRepository;
        this.productMapper = productMapper;
    }

    @Transactional
    public ProductDetailResponse execute(UUID id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException(id));

        var category = categoryRepository.require(request.categoryId());

        product.update(request.name(), request.description(), request.categoryId(),
                request.purchasePrice(), request.active());

        if (Boolean.TRUE.equals(request.composite()) && !product.isComposite()) {
            product.makeComposite();
        }

        if (product.isComposite() && request.components() != null) {
            Set<UUID> seen = new HashSet<>();
            List<ProductComponent> components = request.components().stream().map(r -> {
                if (!seen.add(r.componentProductId())) {
                    throw new DuplicateProductComponentException(r.componentProductId());
                }
                Product comp = productRepository.findById(r.componentProductId())
                    .orElseThrow(() -> new ProductNotFoundException(r.componentProductId()));
                if (comp.isComposite()) {
                    throw new CompositeProductCannotBeComponentException(r.componentProductId());
                }
                return new ProductComponent(UUID.randomUUID(), id, r.componentProductId(), r.quantity());
            }).toList();
            product.setComponents(components);
        }

        if (request.productPrices() != null) {
            List<ProductPrice> productPrices = buildProductPrices(request.productPrices());
            product.setProductPrices(productPrices);
        }

        Product saved = productRepository.save(product);

        Map<UUID, Product> componentProducts = saved.getComponents().stream()
            .map(c -> productRepository.findById(c.getComponentProductId()).orElse(null))
            .filter(p -> p != null)
            .collect(Collectors.toMap(Product::getId, p -> p, (a, b) -> a));

        Map<UUID, String> priceTypeNames = resolvePriceTypeNames(saved.getProductPrices());
        return productMapper.toDetailResponse(saved, category.name(), componentProducts, priceTypeNames);
    }

    private List<ProductPrice> buildProductPrices(List<ProductPriceRequest> requests) {
        if (requests.isEmpty()) {
            return List.of();
        }
        return requests.stream().map(r -> {
            priceTypeRepository.require(r.priceTypeId());
            return ProductPrice.create(r.priceTypeId(), r.price(), r.profitMargin());
        }).toList();
    }

    private Map<UUID, String> resolvePriceTypeNames(List<ProductPrice> prices) {
        return prices.stream()
            .collect(Collectors.toMap(
                ProductPrice::getPriceTypeId,
                pp -> priceTypeRepository.findById(pp.getPriceTypeId())
                    .map(pt -> pt.name())
                    .orElse("")
            ));
    }
}
