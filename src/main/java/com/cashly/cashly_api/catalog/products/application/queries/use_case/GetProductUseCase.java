package com.cashly.cashly_api.catalog.products.application.queries.use_case;

import com.cashly.cashly_api.catalog.products.application.ports.out.ProductCategoryGateway;
import com.cashly.cashly_api.catalog.products.application.ports.out.ProductPriceTypeGateway;
import com.cashly.cashly_api.catalog.products.application.dto.response.ProductDetailResponse;
import com.cashly.cashly_api.catalog.products.application.exceptions.ProductNotFoundException;
import com.cashly.cashly_api.catalog.products.application.mappers.ProductMapper;
import com.cashly.cashly_api.catalog.products.domain.entity.Product;
import com.cashly.cashly_api.catalog.products.domain.repository.ProductRepository;
import com.cashly.cashly_api.catalog.products.domain.valueobjects.ProductPrice;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class GetProductUseCase {

    private final ProductRepository productRepository;
    private final ProductCategoryGateway categoryRepository;
    private final ProductPriceTypeGateway priceTypeRepository;
    private final ProductMapper productMapper;

    public GetProductUseCase(ProductRepository productRepository,
                              ProductCategoryGateway categoryRepository,
                              ProductPriceTypeGateway priceTypeRepository,
                              ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.priceTypeRepository = priceTypeRepository;
        this.productMapper = productMapper;
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse execute(UUID id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException(id));

        String categoryName = categoryRepository.findById(product.getCategoryId())
            .map(c -> c.name()).orElse("");

        Map<UUID, Product> componentProducts = product.getComponents().stream()
            .map(c -> productRepository.findById(c.getComponentProductId()).orElse(null))
            .filter(p -> p != null)
            .collect(Collectors.toMap(Product::getId, p -> p, (a, b) -> a));

        Map<UUID, String> priceTypeNames = product.getProductPrices().stream()
            .collect(Collectors.toMap(
                ProductPrice::getPriceTypeId,
                pp -> priceTypeRepository.findById(pp.getPriceTypeId())
                    .map(pt -> pt.name())
                    .orElse("")
            ));

        return productMapper.toDetailResponse(product, categoryName, componentProducts, priceTypeNames);
    }
}
