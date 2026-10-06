package com.cashly.cashly_api.products.application.usecases;

import com.cashly.cashly_api.categories.domain.repositories.CategoryRepository;
import com.cashly.cashly_api.price_types.domain.repositories.PriceTypeRepository;
import com.cashly.cashly_api.products.application.dto.response.ProductDetailResponse;
import com.cashly.cashly_api.products.application.exceptions.ProductNotFoundException;
import com.cashly.cashly_api.products.application.mappers.ProductMapper;
import com.cashly.cashly_api.products.domain.entities.Product;
import com.cashly.cashly_api.products.domain.repositories.ProductRepository;
import com.cashly.cashly_api.products.domain.valueobjects.ProductPrice;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class GetProductUseCase {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final PriceTypeRepository priceTypeRepository;
    private final ProductMapper productMapper;

    public GetProductUseCase(ProductRepository productRepository,
                              CategoryRepository categoryRepository,
                              PriceTypeRepository priceTypeRepository,
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
            .map(c -> c.getName()).orElse("");

        Map<UUID, Product> componentProducts = product.getComponents().stream()
            .map(c -> productRepository.findById(c.getComponentProductId()).orElse(null))
            .filter(p -> p != null)
            .collect(Collectors.toMap(Product::getId, p -> p, (a, b) -> a));

        Map<UUID, String> priceTypeNames = product.getProductPrices().stream()
            .collect(Collectors.toMap(
                ProductPrice::getPriceTypeId,
                pp -> priceTypeRepository.findById(pp.getPriceTypeId())
                    .map(pt -> pt.getName())
                    .orElse("")
            ));

        return productMapper.toDetailResponse(product, categoryName, componentProducts, priceTypeNames);
    }
}
