package com.cashly.cashly_api.products.infrastructure.persistence.adapters;

import com.cashly.cashly_api.products.infrastructure.persistence.repositories.SpringDataProductCodeSequenceRepository;
import com.cashly.cashly_api.products.infrastructure.persistence.repositories.SpringDataProductRepository;

import com.cashly.cashly_api.shared.infrastructure.pagination.PageResponseMapper;

import com.cashly.cashly_api.products.domain.Product;
import com.cashly.cashly_api.products.application.ports.out.ProductPriceTypeExistenceGateway;
import com.cashly.cashly_api.products.application.ports.out.ProductStockFilterGateway;
import com.cashly.cashly_api.products.application.ports.out.ProductReferenceGateway;
import com.cashly.cashly_api.products.domain.exceptions.ProductReferencedException;
import com.cashly.cashly_api.products.domain.ProductRepository;
import com.cashly.cashly_api.products.domain.valueobjects.ProductPrice;
import com.cashly.cashly_api.products.infrastructure.persistence.entities.ProductCodeSequenceEntity;
import com.cashly.cashly_api.products.infrastructure.persistence.entities.ProductEntity;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
public class JpaProductRepository implements ProductRepository {

    private static final Set<String> ALLOWED_ORDER_BY = Set.of("name", "code", "createdAt", "updatedAt");
    private static final String CODE_PREFIX = "PRD-";
    private static final String CODE_FORMAT = CODE_PREFIX + "%05d";

    private final SpringDataProductRepository springDataRepository;
    private final SpringDataProductCodeSequenceRepository codeSequenceRepository;
    private final ProductPriceTypeExistenceGateway priceTypes;
    private final ProductStockFilterGateway stockFilter;
    private final ProductReferenceGateway references;

    public JpaProductRepository(SpringDataProductRepository springDataRepository,
                                 SpringDataProductCodeSequenceRepository codeSequenceRepository,
                                ProductPriceTypeExistenceGateway priceTypes,
                                ProductStockFilterGateway stockFilter,
                                ProductReferenceGateway references) {
        this.springDataRepository = springDataRepository;
        this.codeSequenceRepository = codeSequenceRepository;
        this.priceTypes = priceTypes;
        this.stockFilter = stockFilter;
        this.references = references;
    }

    @Override
    public Product save(Product product) {
        if (product.getId() != null) {
            return springDataRepository.findWithComponentsById(product.getId())
                .map(entity -> {
                    entity.getComponents().clear();
                    entity.getProductPrices().clear();
                    springDataRepository.saveAndFlush(entity);
                    Set<UUID> existingPriceTypeIds = resolveExistingPriceTypeIds(product.getProductPrices());
                    entity.updateFromDomain(product, existingPriceTypeIds);
                    return springDataRepository.save(entity).toDomain();
                })
                .orElseGet(() -> {
                    ProductEntity entity = ProductEntity.fromDomain(product);
                    entity.setId(UUID.randomUUID());
                    Set<UUID> existingPriceTypeIds = resolveExistingPriceTypeIds(product.getProductPrices());
                    entity.applyPrices(product.getProductPrices(), existingPriceTypeIds);
                    return springDataRepository.save(entity).toDomain();
                });
        }
        ProductEntity entity = ProductEntity.fromDomain(product);
        entity.setId(UUID.randomUUID());
        Set<UUID> existingPriceTypeIds = resolveExistingPriceTypeIds(product.getProductPrices());
        entity.applyPrices(product.getProductPrices(), existingPriceTypeIds);
        return springDataRepository.save(entity).toDomain();
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return springDataRepository.findWithComponentsById(id)
            .map(ProductEntity::toDomain);
    }

    @Override
    public boolean existsByCode(String code) {
        return springDataRepository.existsByCode(code);
    }

    @Override
    public boolean existsByCodeAndIdNot(String code, UUID id) {
        return springDataRepository.existsByCodeAndIdNot(code, id);
    }

    @Override
    public boolean existsByCategoryId(UUID categoryId) {
        return springDataRepository.existsByCategoryId(categoryId);
    }

    @Override
    public PageResponse<Product> findAllWithFilters(String search, UUID categoryId, Boolean active,
                                                     Boolean composite, int page, int size,
                                                     String orderBy, String orderDir) {
        String field = ALLOWED_ORDER_BY.contains(orderBy) ? orderBy : "name";
        Sort sort = "desc".equalsIgnoreCase(orderDir) ? Sort.by(field).descending() : Sort.by(field).ascending();
        PageRequest pageable = PageRequest.of(page, size, sort);

        Page<ProductEntity> result = springDataRepository.findAllWithFilters(
            search, categoryId, active, composite, pageable);

        Page<Product> mapped = result.map(ProductEntity::toDomain);
        return PageResponseMapper.from(mapped);
    }

    @Override
    public PageResponse<Product> findAllForOrder(String search, UUID categoryId, Boolean composite,
                                                  Boolean withStock, Set<UUID> allowedWarehouseIds,
                                                  int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("name").ascending());

        Page<ProductEntity> result;
        if (withStock == null) {
            result = springDataRepository.findAllForOrder(search, categoryId, composite, pageable);
        } else {
            Set<UUID> productIds = stockFilter.availableProductIds(allowedWarehouseIds);
            if (productIds.isEmpty()) {
                result = withStock ? Page.empty(pageable)
                        : springDataRepository.findAllForOrder(search, categoryId, composite, pageable);
            } else {
                result = withStock
                        ? springDataRepository.findAllForOrderWithStock(search, categoryId, composite, productIds, pageable)
                        : springDataRepository.findAllForOrderWithoutStock(search, categoryId, composite, productIds, pageable);
            }
        }

        Page<Product> mapped = result.map(ProductEntity::toDomain);
        return PageResponseMapper.from(mapped);
    }

    @Override
    public void delete(UUID id) {
        try {
            springDataRepository.deleteById(id);
        } catch (DataIntegrityViolationException exception) {
            throw new ProductReferencedException(id, exception);
        }
    }

    @Override
    public void deleteComponentReferencesByProductId(UUID productId) {
        springDataRepository.deleteByComponentProductId(productId);
    }

    @Override
    public Set<UUID> findIdsWithReferences(Set<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return Set.of();
        }
        Set<UUID> referenced = new HashSet<>(springDataRepository.findProductIdsWithComponentReferences(ids));
        referenced.addAll(references.findReferencedProductIds(ids));
        return Set.copyOf(referenced);
    }

    @Override
    public Set<UUID> findIdsByCategoryId(UUID categoryId) {
        return Set.copyOf(springDataRepository.findIdsByCategoryId(categoryId));
    }

    @Override
    public String generateNextCode() {
        ProductCodeSequenceEntity sequence = codeSequenceRepository.save(new ProductCodeSequenceEntity());
        return String.format(CODE_FORMAT, sequence.getId());
    }

    private Set<UUID> resolveExistingPriceTypeIds(List<ProductPrice> prices) {
        if (prices == null || prices.isEmpty()) {
            return Set.of();
        }
        List<UUID> priceTypeIds = prices.stream()
            .map(ProductPrice::getPriceTypeId)
            .distinct()
            .toList();
        return priceTypes.existingIds(priceTypeIds);
    }
}
