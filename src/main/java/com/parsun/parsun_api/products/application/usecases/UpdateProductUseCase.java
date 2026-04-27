package com.parsun.parsun_api.products.application.usecases;

import com.parsun.parsun_api.categories.application.exceptions.CategoryNotFoundException;
import com.parsun.parsun_api.categories.domain.repositories.CategoryRepository;
import com.parsun.parsun_api.products.application.dto.request.UpdateProductRequest;
import com.parsun.parsun_api.products.application.dto.response.ProductDetailResponse;
import com.parsun.parsun_api.products.application.exceptions.CompositeProductCannotBeComponentException;
import com.parsun.parsun_api.products.application.exceptions.ProductNotFoundException;
import com.parsun.parsun_api.products.application.mappers.ProductMapper;
import com.parsun.parsun_api.products.domain.entities.Product;
import com.parsun.parsun_api.products.domain.entities.ProductComponent;
import com.parsun.parsun_api.products.domain.repositories.ProductRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class UpdateProductUseCase {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    public UpdateProductUseCase(ProductRepository productRepository,
                                 CategoryRepository categoryRepository,
                                 ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productMapper = productMapper;
    }

    @Transactional
    public ProductDetailResponse execute(UUID id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException(id));

        var category = categoryRepository.findById(request.categoryId())
            .orElseThrow(() -> new CategoryNotFoundException(request.categoryId()));

        product.update(request.name(), request.description(), request.categoryId(),
                request.basePrice(), request.purchasePrice(), request.profitMargin());

        if (product.isComposite() && request.components() != null) {
            List<ProductComponent> components = request.components().stream().map(r -> {
                Product comp = productRepository.findById(r.componentProductId())
                    .orElseThrow(() -> new ProductNotFoundException(r.componentProductId()));
                if (comp.isComposite()) {
                    throw new CompositeProductCannotBeComponentException(r.componentProductId());
                }
                return new ProductComponent(null, id, r.componentProductId(), r.quantity());
            }).toList();
            product.setComponents(components);
        }

        Product saved = productRepository.save(product);

        Map<UUID, Product> componentProducts = saved.getComponents().stream()
            .map(c -> productRepository.findById(c.getComponentProductId()).orElse(null))
            .filter(p -> p != null)
            .collect(Collectors.toMap(Product::getId, p -> p));

        return productMapper.toDetailResponse(saved, category.getName(), componentProducts);
    }
}
