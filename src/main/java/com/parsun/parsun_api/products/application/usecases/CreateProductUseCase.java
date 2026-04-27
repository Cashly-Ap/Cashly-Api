package com.parsun.parsun_api.products.application.usecases;

import com.parsun.parsun_api.categories.application.exceptions.CategoryNotFoundException;
import com.parsun.parsun_api.categories.domain.repositories.CategoryRepository;
import com.parsun.parsun_api.products.application.dto.request.CreateProductRequest;
import com.parsun.parsun_api.products.application.dto.response.ProductDetailResponse;
import com.parsun.parsun_api.products.application.exceptions.CompositeProductCannotBeComponentException;
import com.parsun.parsun_api.products.application.exceptions.DuplicateProductCodeException;
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

public class CreateProductUseCase {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    public CreateProductUseCase(ProductRepository productRepository,
                                 CategoryRepository categoryRepository,
                                 ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productMapper = productMapper;
    }

    @Transactional
    public ProductDetailResponse execute(CreateProductRequest request) {
        if (productRepository.existsByCode(request.code().trim().toUpperCase())) {
            throw new DuplicateProductCodeException(request.code());
        }

        var category = categoryRepository.findById(request.categoryId())
            .orElseThrow(() -> new CategoryNotFoundException(request.categoryId()));

        Product product = Product.create(request.code(), request.name(), request.description(),
                request.categoryId(), Boolean.TRUE.equals(request.composite()),
                request.basePrice(), request.purchasePrice(), request.profitMargin());

        Product saved = productRepository.save(product);

        if (Boolean.TRUE.equals(request.composite()) && request.components() != null && !request.components().isEmpty()) {
            List<ProductComponent> components = buildComponents(saved.getId(), request.components());
            saved.setComponents(components);
            saved = productRepository.save(saved);
        }

        Map<UUID, Product> componentProducts = loadComponentProducts(saved);
        return productMapper.toDetailResponse(saved, category.getName(), componentProducts);
    }

    private List<ProductComponent> buildComponents(UUID parentId,
            List<CreateProductRequest.ComponentRequest> requests) {
        return requests.stream().map(r -> {
            Product comp = productRepository.findById(r.componentProductId())
                .orElseThrow(() -> new ProductNotFoundException(r.componentProductId()));
            if (comp.isComposite()) {
                throw new CompositeProductCannotBeComponentException(r.componentProductId());
            }
            return new ProductComponent(null, parentId, r.componentProductId(), r.quantity());
        }).toList();
    }

    private Map<UUID, Product> loadComponentProducts(Product product) {
        return product.getComponents().stream()
            .map(c -> productRepository.findById(c.getComponentProductId()).orElse(null))
            .filter(p -> p != null)
            .collect(Collectors.toMap(Product::getId, p -> p));
    }
}
